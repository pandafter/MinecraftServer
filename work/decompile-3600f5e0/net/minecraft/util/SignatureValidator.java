package net.minecraft.util;

import com.mojang.authlib.yggdrasil.ServicesKeyInfo;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.logging.LogUtils;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public interface SignatureValidator {

    SignatureValidator NO_VALIDATION = (signatureupdater, abyte) -> {
        return true;
    };
    Logger LOGGER = LogUtils.getLogger();

    boolean validate(SignatureUpdater signatureupdater, byte[] abyte);

    default boolean validate(byte[] abyte, byte[] abyte1) {
        return this.validate((signatureupdater_a) -> {
            signatureupdater_a.update(abyte);
        }, abyte1);
    }

    private static boolean verifySignature(SignatureUpdater signatureupdater, byte[] abyte, Signature signature) throws SignatureException {
        Objects.requireNonNull(signature);
        signatureupdater.update(signature::update);
        return signature.verify(abyte);
    }

    static SignatureValidator from(PublicKey publickey, String s) {
        return (signatureupdater, abyte) -> {
            try {
                Signature signature = Signature.getInstance(s);

                signature.initVerify(publickey);
                return verifySignature(signatureupdater, abyte, signature);
            } catch (Exception exception) {
                SignatureValidator.LOGGER.error("Failed to verify signature", exception);
                return false;
            }
        };
    }

    @Nullable
    static SignatureValidator from(ServicesKeySet serviceskeyset, ServicesKeyType serviceskeytype) {
        Collection<ServicesKeyInfo> collection = serviceskeyset.keys(serviceskeytype);

        return collection.isEmpty() ? null : (signatureupdater, abyte) -> {
            return collection.stream().anyMatch((serviceskeyinfo) -> {
                Signature signature = serviceskeyinfo.signature();

                try {
                    return verifySignature(signatureupdater, abyte, signature);
                } catch (SignatureException signatureexception) {
                    SignatureValidator.LOGGER.error("Failed to verify Services signature", signatureexception);
                    return false;
                }
            });
        };
    }
}
