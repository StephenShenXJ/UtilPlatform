package com.shen.stephen.utilplatform.widget.fingerprint;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.shen.stephen.utilplatform.R;
import com.shen.stephen.utilplatform.widget.PkiActivity;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Created by Xiao on 3/4/16.
 */
public class OSFingerprintHelper {
    private static final String DIALOG_FRAGMENT_TAG = "FingerprintDialog";
    private static final String SECRET_MESSAGE = "PivotSecretMessage";
    /**
     * Alias for our key in the Android Key Store
     */
    private static final String KEY_NAME = "pivotFingerprintKey";

    private Context mContext;

    KeyguardManager mKeyguardManager;
    FingerprintManagerCompat mFingerprintManager;
    FingerprintAuthenticationDialogFragment mFragment;
    KeyStore mKeyStore;
    KeyGenerator mKeyGenerator;
    Cipher mCipher;
    SharedPreferences mSharedPreferences;

    public boolean forceHiddenPasswordButton;
    public boolean fingerprintUserful;

    private enum fingerprintError{
        NoSetupFingerprintOrlockScreen,
        NoRigresterFingerprint;
    }
    fingerprintError fError;

    public OSFingerprintHelper(PkiActivity activity) {
        init(activity);
    }

    public void init(Context context) {
        mContext = context;
        forceHiddenPasswordButton = false;
        fingerprintUserful = true;
        setOSPFingerprintHelper(context);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setOSPFingerprintHelper(Context context){
        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }

        try {
            mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }

        if (!mKeyguardManager.isKeyguardSecure()) {
            fError = fingerprintError.NoSetupFingerprintOrlockScreen;
            fingerprintUserful = false;
            // Show a message that the user hasn't set up a fingerprint or lock screen.
            fingerprintErrorToast();
            return;
        }

        mFingerprintManager = FingerprintManagerCompat.from(context);

        //noinspection ResourceType
        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            fError = fingerprintError.NoRigresterFingerprint;
            fingerprintUserful = false;
            // This happens when no fingerprints are registered.
            fingerprintErrorToast();
            return;
        }
        createKey();
        if (mFragment == null) {
            mFragment = new FingerprintAuthenticationDialogFragment();
        }
    }

    public void fingerprintErrorToast(){
        int toastStr;
        if (fError == fingerprintError.NoSetupFingerprintOrlockScreen){
            toastStr = R.string.fingerprint_secure_lock_screen_has_not_set_up;
        } else
        if (fError == fingerprintError.NoRigresterFingerprint){
            toastStr = R.string.fingerprint_has_not_register;
        } else {
            return;
        }
        Toast.makeText(mContext,
                toastStr,
                Toast.LENGTH_SHORT).show();
    }

    public void startFingerprintDialog(PkiActivity activity) {
        // Set up the crypto object for later. The object will be authenticated by use
        // of the fingerprint.
        mFragment.forceHiddenPasswordButton = forceHiddenPasswordButton;
        if (initCipher()) {
            // Show the fingerprint dialog. The user has the option to use the fingerprint with
            // crypto, or you can fall back to using a server-side verified password.
            mFragment.setCryptoObject(new FingerprintManagerCompat.CryptoObject(mCipher));
            mFragment.setStage(FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
            activity.showFragmentDialog(mFragment, DIALOG_FRAGMENT_TAG);
            //mFragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
        } else {
            // This happens if the lock screen has been disabled or or a fingerprint got
            // enrolled. Thus show the dialog to authenticate with their password first
            // and ask the user if they want to authenticate with fingerprints in the
            // future
            mFragment.setCryptoObject(new FingerprintManagerCompat.CryptoObject(mCipher));
            mFragment.setStage(
                    FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED);
            //mFragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            activity.showFragmentDialog(mFragment, DIALOG_FRAGMENT_TAG);
        }
    }

    public void dismissFingerprintDialog() {
        mFragment.dismiss();
    }

    /**
     * Initialize the {@link Cipher} instance with the created key in the {@link #createKey()}
     * method.
     *
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    private boolean initCipher() {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_NAME, null);
            mCipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }



    /**
     * Tries to encrypt some data with the generated key in {@link #createKey} which is
     * only works if the user has just authenticated via fingerprint.
     */
    public void tryEncrypt() {
        try {
            byte[] encrypted = mCipher.doFinal(SECRET_MESSAGE.getBytes());
        } catch (BadPaddingException | IllegalBlockSizeException e) {
//            Toast.makeText(mContext, "Failed to encrypt the data with the generated key. "
//                    + "Retry the validation", Toast.LENGTH_LONG).show();
            Log.e("", "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void createKey() {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            mKeyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
