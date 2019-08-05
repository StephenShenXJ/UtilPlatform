/**
 *
 * DESKeyEncrypt.java
 * PIVOT
 * Created by Arathy on 21-Mar-2014
 * Copyright 2014 PerkinElmer. All rights reserved.
 * */
package com.shen.stephen.utilplatform.util.encryption;

import android.util.Base64;

import com.shen.stephen.utilplatform.log.PLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DESKeyEncrypt {

	private static String sResult = "";

	public static String encrypt(String sData) throws GeneralSecurityException, IOException {
		try {

			String sKey = "encKey";

			// Generate the keys
			byte[] bytKey = new byte[sKey.length()];
			bytKey = sKey.getBytes("ISO646-US");

			PLog.v("bytKey" + bytKey.toString());

			// Hash the key using SHA1
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");

			byte[] bytHash = sha1.digest(bytKey);

			int i;
			byte[] m_Key = new byte[8];
			byte[] m_IV = new byte[8];
			// use the low 64-bits for the key value
			for (i = 0; i < 8; i++)
				m_Key[i] = bytHash[i];

			for (i = 8; i < 16; i++)
				m_IV[i - 8] = bytHash[i];

			/*
			 * Prepare the String The first 5 character of the string is
			 * formatted to store the actual length of the data. This is the
			 * simplest way to remember to original length of the data, without
			 * resorting to complicated computations.
			 */

			int sLen = sData.length();
			String sDataNew = String.format("%05d", sLen);

			sData = sDataNew + sData;

			// sData = String.format ("%1$"+sData.length()+ "0", sData);
			// Encrypt the Data
			byte[] bytData = new byte[sData.length()];
			bytData = sData.getBytes();

			KeySpec keySpec = new DESKeySpec(m_Key);
			SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(
					keySpec);

			IvParameterSpec iv = new IvParameterSpec(m_IV);
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			byte[] encoded = cipher.doFinal(sData.getBytes("ASCII"));
			String result = Base64.encodeToString(encoded, Base64.DEFAULT);
			PLog.v("cipher:" + result);

			/*
			 * Prepare the streams: / objMemOutputStream is the output stream. /
			 * objMemInputStream is the input stream. / objCryptoStream is the
			 * transformation stream. / MemoryStream objMemInputStream = new
			 * MemoryStream(bytData);
			 */
			ByteArrayInputStream objMemInputStream = new ByteArrayInputStream(
					bytData);

			ByteArrayOutputStream objMemOutputStream = new ByteArrayOutputStream();

			// performing the encryption

			CipherInputStream cryptoStream = new CipherInputStream(
					objMemInputStream, cipher);
			byte[] buffer = new byte[1024];
			int len = cryptoStream.read(buffer, 0, buffer.length);
			while (len > 0) {
				objMemOutputStream.write(buffer, 0, len);
				len = cryptoStream.read(buffer, 0, buffer.length);
			}

			/*
			 * Returns the encrypted result after it is base64 encoded In this
			 * case, the actual result is converted to base64 so that it can be
			 * transported over the HTTP protocol without deformation.
			 */
			if (objMemOutputStream.size() == 0)
				sResult = "";
			else

				sResult = Base64.encodeToString(
						objMemOutputStream.toByteArray(), Base64.DEFAULT);

			sResult = sResult.replaceAll("(\\r|\\n)", "");

		} catch (UnsupportedEncodingException e) {
			PLog.e("Exception .. " + e.getMessage());
		}
		PLog.v("Encoded string:" + sResult);
		return sResult;
	}

}
