public class Utils {
    private static String toBinary(int decimal) {
	String binary = "";
	while (decimal > 0) {
	    binary = decimal % 2 + binary;
	    decimal /= 2;
	}

	// Pad binary number until length is 8
	while (binary.length() < 8) {
	    binary = "0" + binary;
	}
	return binary;
    }

    private static String addBinary(String b1, String b2) {
	String total = "";
	int carryOver =  0;
	for (int i = 7; i >= 0; i--) {
	    int sum = Character.getNumericValue(b1.charAt(i)) + Character.getNumericValue(b2.charAt(i)) + carryOver;
	    carryOver = (sum > 1) ? 1: 0;
	    total = sum % 2 + total;
	}

	// If result is 9 digits, wrap the extra 1 around by adding 1 to the first digit.
	if (carryOver == 1) {
	    total = addBinary(total, "00000001");
	}
	return total;
    }

    private static int binaryToDecimal(String binary) {
	int decimal = 0;
	for (int i = 0; i < binary.length(); i++) {
	    decimal += (int) Math.pow(2, i) * Character.getNumericValue(binary.charAt(binary.length()-1-i));
	}
	return decimal;
    }
    public static String encrypt(String userName, String password) {
	String plainText = "";
	String key = "";

	// Create plain text, which will be converted using the algorithm, and consists of
	// userName repeated until matching the length of userName + password
	// Also create the key, which will be used to convert the plain text in the algorithm,
	// and consists of password repeated in the same way.
	for (int i = 0; key.length() < userName.length() + password.length(); i++) {
	    plainText += userName.charAt(i % userName.length());
	    key += password.charAt(i % password.length());
	}

	String encryptedOutput = "";
	String fingerprintBase = "00000000";
	for (int i = 0; i < userName.length(); i++) {
	    fingerprintBase = addBinary(fingerprintBase, toBinary(userName.charAt(i)));
	}

	// Convert each character in plainText into a unique hex number using key.
	for (int i = 0; i < plainText.length(); i++) {
	    String plainTextBinary = toBinary(plainText.charAt(i));
	    String keyBinary = toBinary((key.charAt(i)));
	    String encrypted = addBinary(plainTextBinary, keyBinary);
	    // Unique "fingerprint" to ensure the sum of two different pairs of binary numbers
	    // cannot result in the same binary number
	    String fingerprint = addBinary(fingerprintBase, toBinary(userName.charAt(i % userName.length())));
	    // Concatenate encrypted and fingerprint and convert to hexadecimal to reduce size
	    encryptedOutput += Integer.toHexString(binaryToDecimal(encrypted + fingerprint));
	}

	return encryptedOutput;
    }
    
    public static String intToBytesStr(int num) {
	return "" + (char) (num >> 24) + 
		    (char) ((num & 0xFF0000) >> 16) + 
		    (char) ((num & 0xFF00) >> 8) +
		    (char) ((num & 0xFF));
    }
    
    public static String intToByteStr(int num) { 
	return intToBytesStr(num);
    }
    public static void delay(long ms) {
	MailTransfers.sleep(ms);
    }
    // convert a base 256 8-byte string to an 8 byte long
    public static long bytesStrToLong(String str) {
	return ((long) (str.charAt(0)) << 56) |
	    ((long) (str.charAt(1)) << 48) |
	    ((long) (str.charAt(2)) << 40) |
	    ((long) (str.charAt(3)) << 32) |
	    ((long) (str.charAt(4)) << 24) |
	    ((long) (str.charAt(5)) << 16) |
	    ((long) (str.charAt(6)) << 8) |
	    ((long) (str.charAt(7)));
    }
}
