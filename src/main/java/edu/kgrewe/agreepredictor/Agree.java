package edu.kgrewe.agreepredictor;

import java.util.ArrayList;

public class Agree extends BranchPredictor {
	private String GHR;
	private StringBuilder newPC;
	private ArrayList<ArrayList<String>> BBS;
	private LRU least_recently_used_BBS;
	private final int BBS_size = 4096;

	public Agree() {
		super();
		GHR = "0";
		newPC = null;
		BBS = new ArrayList<ArrayList<String>>();
		least_recently_used_BBS = new LRU(BBS_size);

		// Initialize BBS.
		for (int i = 0; i < BBS_size; i++) {
			ArrayList<String> row = new ArrayList<String>();
			row.add("-1");
			row.add("-1");
			BBS.add(row);
		}
	}

	public Agree(long table_size, long counter_bits) {
		super(table_size, counter_bits);
		GHR = "0";
		newPC = null;
		BBS = new ArrayList<ArrayList<String>>();
		least_recently_used_BBS = new LRU(BBS_size);

		// Initialize BBS.
		for (int i = 0; i < BBS_size; i++) {
			ArrayList<String> row = new ArrayList<String>();
			row.add("-1");
			row.add("-1");
			BBS.add(row);
		}
	}

	@Override
	public Predict prediction(String address, String result) {
		// Perform the GShare of PC and GHR to index PHT.
		String xor = getXOR(address);
		access(xor);
		boolean found = false;
		int index = -1;
		ArrayList<ArrayList<String>> PHT = getPHT();

//		System.out.println("\n================================================================");
//		System.out.println("================================================================");
//		System.out.println("Predicting branch " + address + " with actual result " + result);
//		System.out.println("GHR: " + GHR);
//		System.out.println("PC: " + address);
//		System.out.println("XOR: " + getXOR(address));
//		System.out.println("[PHT & BBS Before]");
//		printPHT();
//		printBBS();

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
			if (PHT_value.compareTo(getMin_value()) < 0) {
				PHTBit = false;
			} else {
				PHTBit = true;
			}
		} else {
			index = replaceLRUPHT(xor);
		}

		int BBS_index = -1;
		String tag = address.substring(0, address.length()-4);
		// Check the Biasing Bit Storage.
		found = false;
		for (int i = 0; i < BBS_size; i++) {
			if (BBS.get(i).get(0).equals(tag)) {
				found = true;
				BBS_index = i;
				break;
			}
		}

		boolean BBSBit = false;
		if (found) {
			String BBS_value = BBS.get(BBS_index).get(1);
			if (BBS_value.equals("1")) {
				BBSBit = true;
			} else {
				BBSBit = false;
			}
		} else {
			// If not found, use lowest bit as BBSBit.
			replaceLRUBBS(tag, result);
			BBSBit = getLowest_bit(address);
			System.out.println("[PHT & BBS After]");
			printPHT();
			printBBS();
		}

		updateGHR(result);
		boolean xnor_result = getXNOR(PHTBit, BBSBit);

		// Increment PHT counter if bits agree,
		// Otherwise decrement.
		if (xnor_result == true) {
			updatePHTState(index, xor, "T");
			 System.out.println("[PHT & BBS After]");
			printPHT();
			printBBS();
			System.out.println("Predict TRUE");
			return Predict.TRUE;
		} else {
			updatePHTState(index, xor, "F");
			System.out.println("[PHT & BBS After]");
			printPHT();
			printBBS();
			System.out.println("Predict FALSE");
			return Predict.FALSE;
		}
	}

	private void updateGHR(String result) {
		// If GHR is 10 bits, remove the oldest bit.
		if (GHR.length() == 10) {
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
		newPC = null;

		// Ensure the address is legit.
		try {
			newPC = new StringBuilder(Utility.hexToBin(address));
		} catch (Exception e) {
			System.out.println("Error converting address to binary.");
		}

		// System.out.println("Binary PC" + newPC);

//		if (newPC.length() < 64) {
//			int add = 64 - newPC.length();
//			for (int i = 0; i < add; i++) {
//				newPC.insert(0, "0");
//			}
//		}

		String finalPC = newPC.toString();
		int endLength = newPC.length() - 6;
		// System.out.println("Before trim" + finalPC);
		finalPC = finalPC.substring(endLength - 10, endLength);
		// System.out.println("After trim" + finalPC);
		return Integer.toBinaryString(((Integer.parseInt(GHR, 2)) ^ (Integer.parseInt(finalPC, 2))));
	}

	private boolean getXNOR(boolean PHTbit, boolean BTBbit) {
		return !(PHTbit ^ BTBbit);
	}

	private void replaceLRUBBS(String address, String result) {
		int replace = -1;
		boolean full = true;

		for (int i = 0; i < BBS_size; i++) {
			if (BBS.get(i).get(0).equals("-1")) {
				replace = i;
				full = false;
				break;
			}
		}
		if (full) {
			String lru = least_recently_used_BBS.getLRU();
			// System.out.println("BBS LRU " + lru);
			for (int i = 0; i < BBS_size; i++) {
				if (BBS.get(i).get(0).equals(lru)) {
					replace = i;
					break;
				}
			}
		}

		if (replace < 0) {
			replace = BBS_size - 1;
		}

		// System.out.println("Replacing BBS index " + replace + " with " + address);
		BBS.get(replace).set(0, address);
		if (result.equals("T") || result.equals("1")) {
			BBS.get(replace).set(1, "1");
		} else {
			BBS.get(replace).set(1, "0");
		}
	}

	private void printBBS() {
		System.out.println("-----------Biasing Bit Storage--------");
		for (int i = 0; i < BBS_size; i++) {
			if (BBS.get(i).get(0).equals("-1")) {
				break;
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
