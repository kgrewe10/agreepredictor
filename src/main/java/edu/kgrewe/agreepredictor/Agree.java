package edu.kgrewe.agreepredictor;

import java.math.BigInteger;
import java.util.ArrayList;

public class Agree extends BranchPredictor {
	private String GHR;
	private final int GHR_SIZE = 10;

	private ArrayList<ArrayList<String>> BBS;
	private final int BBS_SIZE = 4096;

	public Agree() {
		super();
		GHR = "0";
		BBS = new ArrayList<ArrayList<String>>();

		// Initialize BBS.
		for (int i = 0; i < BBS_SIZE; i++) {
			ArrayList<String> row = new ArrayList<String>();
			row.add("-1");
			row.add("-1");
			BBS.add(row);
		}
	}

	public Agree(long table_size, long counter_bits) {
		super(table_size, counter_bits);
		GHR = "0";
		BBS = new ArrayList<ArrayList<String>>();

		// Initialize BBS.
		for (int i = 0; i < BBS_SIZE; i++) {
			ArrayList<String> row = new ArrayList<String>();
			row.add("-1");
			row.add("-1");
			BBS.add(row);
		}
	}

	@Override
	public Predict prediction(String address, String result) {

//		System.out.println("\n================================================================");
//		System.out.println("Predicting branch " + address + " with actual result " + result);
		String xor = getXOR(address);
		updateGHR(result);
//		System.out.println("XOR: " + xor);
//		printPHT();
//		printBBS();

		// Perform the GShare of PC and GHR to index PHT.
		access(xor);
		boolean found = false;
		int index = -1;
		ArrayList<ArrayList<String>> PHT = getPHT();

		// Check the PHT for the index using xor.
		for (int i = 0; i < PHT.size(); i++) {
			if (PHT.get(i).get(0).equals(xor)) {
				found = true;
				index = i;
				break;
			}
		}

		// If found in PHT, get the PHTBit.
		boolean PHTBit = false;
		if (found) {
			String PHT_value = PHT.get(index).get(1);

			// Determine the PHT bit.
			if (Integer.parseInt(PHT_value) <= Integer.parseInt(getMin_value())) {
				PHTBit = false;
			} else {
				PHTBit = true;
			}
		} else {
			index = replaceLRUPHT(xor);
		}

		// Get the modulus of the address
		BigInteger addr = new BigInteger(Utility.hexToDec(address));
		BigInteger mod = addr.mod(BigInteger.valueOf(BBS_SIZE));
		int modInt = mod.intValue();

		// Check the BBS for the address, if not found use the lowest bit.
		// System.out.println(addr + " mod " + BBS_SIZE + " = " + mod);
		boolean BBSBit = false;
		if (BBS.get(modInt).get(0).equals(address)) {
			String BBS_value = BBS.get(modInt).get(1);
			if (BBS_value.equals("1")) {
				BBSBit = true;
			} else {
				BBSBit = false;
			}
		} else {
			// Use lowest bit if not found.
			BBSBit = getLowest_bit(address);
			replaceDirectBBS(address, modInt, result);
		}
		boolean xnor_result = getXNOR(PHTBit, BBSBit);

		// If bits agree predict true and update PHT,
		// Else predict false.
		if (xnor_result == true) {
			updatePHTState(index, xor, result);
			// System.out.println("Predict TRUE");
			// System.out.println("================================================================\n");
			return Predict.TRUE;
		} else {
			// System.out.println("Didn't update PHT, bits don't agree");
			// System.out.println("Predict FALSE");
			// System.out.println("================================================================\n");
			return Predict.FALSE;
		}
	}

	private void updateGHR(String result) {
		// If GHR is 10 bits, remove the oldest bit.
		if (GHR.length() == GHR_SIZE) {
			// System.out.println("Before shift " + GHR);
			GHR = GHR.substring(1, GHR.length());
			// System.out.println("After shift " + GHR);
		}

		if (result.equals("T") || result.equals("1")) {
			GHR += "1";
		} else {
			GHR += "0";
		}
	}

	private String getXOR(String address) {
		StringBuilder newPC = null;

		// Convert address to binary to perform XOR.
		try {
			newPC = new StringBuilder(Utility.hexToBin(address));
		} catch (Exception e) {
			System.out.println("Error converting address to binary.");
		}
		// System.out.println("Binary PC: " + newPC);
		// System.out.println("GHR: " + GHR);

		// Get the middle bits to PC to XOR.
		int endLength = newPC.length() - 6;
		String finalPC = newPC.substring(endLength - GHR_SIZE, endLength);

		// System.out.println(finalPC + " XOR " + GHR + " performed.");
		return Integer.toBinaryString(Integer.parseInt(GHR, 2) ^ Integer.parseInt(finalPC, 2));
	}

	private boolean getXNOR(boolean PHTbit, boolean BTBbit) {
		// System.out.println(PHTbit + " XNOR " + BTBbit + " = " + !(PHTbit ^ BTBbit));
		return !(PHTbit ^ BTBbit);
	}

	private void replaceDirectBBS(String address, int mod, String result) {
		// System.out.println("Replacing BBS index with mod result " + mod + " with " +
		// address);
		BBS.get(mod).set(0, address);
		if (result.equals("T") || result.equals("1")) {
			BBS.get(mod).set(1, "1");
		} else {
			BBS.get(mod).set(1, "0");
		}
	}

	private void printBBS() {
		System.out.println("-----------Biasing Bit Storage--------");
		for (int i = 0; i < BBS_SIZE; i++) {
			if (BBS.get(i).get(0).equals("-1")) {
				continue;
			}
			System.out.println(BBS.get(i).get(0) + " --> " + BBS.get(i).get(1));
		}
	}

	private boolean getLowest_bit(String address) {
		String bin = Utility.hexToBin(address);
		String lowestBit = bin.substring(bin.length() - 1);
		if (lowestBit.equals("1")) {
			return true;
		} else {
			return false;
		}
	}
}
