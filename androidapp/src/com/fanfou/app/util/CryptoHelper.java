package com.fanfou.app.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.fanfou.app.App;

public final class CryptoHelper {
	private static final String EncodeAlgorithm = "DES";
	private static final String HEX = "0123456789ABCDEF";
	private static final String SECURE_KEY = "com.fanfou.app.#ertf%[$^(:_+@<>a$%nfo0}4u.c&%om$%";
	private static CryptoHelper instance = null;

	public static CryptoHelper getInstance() {
		if (instance == null) {
			instance = new CryptoHelper();
			if (!instance.init()) {
				instance = null;
			}
		}
		return instance;
	}

	private SecretKey key = null;

	private boolean init() {
		try {
			DESKeySpec desKeySpec = new DESKeySpec(SECURE_KEY.getBytes());
			SecretKeyFactory skf = SecretKeyFactory
					.getInstance(EncodeAlgorithm);
			key = skf.generateSecret(desKeySpec);
		} catch (Exception e) {
			if (App.DEBUG)
				e.printStackTrace();
		}
		return key != null;
	}

	@SuppressWarnings("unused")
	private boolean init2() {
		try {
			KeyGenerator keygen = KeyGenerator.getInstance(EncodeAlgorithm);
			SecureRandom random = new SecureRandom();
			keygen.init(random);
			key = keygen.generateKey();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return key != null;
	}

	private Cipher getCipher(int mode) {
		try {
			Cipher cipher = Cipher.getInstance(EncodeAlgorithm);
			cipher.init(mode, key);
			return cipher;
		} catch (Exception e) {
			if (App.DEBUG)
				e.printStackTrace();
		}
		return null;
	}

	public Cipher getEncodeCipher() {
		return this.getCipher(Cipher.ENCRYPT_MODE);
	}

	public Cipher getDecodeCipher() {
		return this.getCipher(Cipher.DECRYPT_MODE);
	}

	/**
	 * 解密，若输入为null或加/解密过程出现异常，则输出为null <br/>
	 * 作者：wallimn　时间：2009-8-12　上午08:09:44<br/>
	 * 博客：http://wallimn.iteye.com<br/>
	 * 参数：<br/>
	 * 
	 * @param str
	 * @return
	 */
	public String decode(String str) {
		if (str == null)
			return null;
		Cipher cipher = getDecodeCipher();
		StringBuffer sb = new StringBuffer();
		int blockSize = cipher.getBlockSize();
		int outputSize = cipher.getOutputSize(blockSize);
		byte[] src = stringToBytes(str);
		byte[] outBytes = new byte[outputSize];
		int i = 0;
		try {
			for (; i <= src.length - blockSize; i = i + blockSize) {
				int outLength = cipher.update(src, i, blockSize, outBytes);
				sb.append(new String(outBytes, 0, outLength));
			}
			if (i == src.length)
				outBytes = cipher.doFinal();
			else {
				outBytes = cipher.doFinal(src, i, src.length - i);
			}
			sb.append(new String(outBytes));
			return sb.toString();
		} catch (Exception e) {
			if (App.DEBUG)
				e.printStackTrace();
		}
		return null;
	}

	/**
	 * 加密，若输入为null或加/解密过程出现异常，则输出为null <br/>
	 * 作者：wallimn　时间：2009-8-12　上午08:09:59<br/>
	 * 博客：http://wallimn.iteye.com<br/>
	 * 参数：<br/>
	 * 
	 * @param str
	 * @return
	 */
	public String encode(String str) {
		if (str == null)
			return null;
		Cipher cipher = getEncodeCipher();
		StringBuffer sb = new StringBuffer();
		int blockSize = cipher.getBlockSize();
		int outputSize = cipher.getOutputSize(blockSize);
		byte[] src = str.getBytes();
		byte[] outBytes = new byte[outputSize];
		int i = 0;
		try {
			for (; i <= src.length - blockSize; i = i + blockSize) {
				int outLength = cipher.update(src, i, blockSize, outBytes);
				sb.append(bytesToString(outBytes, outLength));
			}
			if (i == src.length)
				outBytes = cipher.doFinal();
			else {
				outBytes = cipher.doFinal(src, i, src.length - i);
			}
			sb.append(bytesToString(outBytes));
			return sb.toString();
		} catch (Exception e) {
			if (App.DEBUG)
				e.printStackTrace();
		}
		return null;
	}

	private String bytesToString(byte[] bs) {
		if (bs == null || bs.length == 0)
			return "";
		return bytesToString(bs, bs.length);
	}

	private String bytesToString(byte[] bs, int len) {
		if (bs == null || bs.length == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			// System.out.println(bs[i]+":"+String.format("%02X", bs[i]));
			sb.append(String.format("%02X", bs[i]));
		}
		return sb.toString();
	}

	private byte[] stringToBytes(String str) {
		if (str == null || str.length() < 2 || str.length() % 2 != 0)
			return new byte[0];
		int len = str.length();
		byte[] bs = new byte[len / 2];
		for (int i = 0; i * 2 < len; i++) {
			bs[i] = (byte) (Integer.parseInt(str.substring(i * 2, i * 2 + 2),
					16) & 0xFF);
			// System.out.println(str.substring(i * 2, i * 2 + 2)+":"+bs[i]);
		}
		return bs;
	}

	public static String encrypt(String seed, String cleartext)
			throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}

	public static String decrypt(String seed, String encrypted)
			throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}

	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
		kgen.init(128, sr); // 192 and 256 bits may not be available
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();
		return raw;
	}

	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted)
			throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String toHex(String txt) {
		return toHex(txt.getBytes());
	}

	public static String fromHex(String hex) {
		return new String(toByte(hex));
	}
	
	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
	}

	public static byte[] toByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
					16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2 * buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}

}
