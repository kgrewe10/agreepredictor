package edu.kgrewe.agreepredictor;

import java.util.ArrayList;

public class Agree extends BranchPredictor {
	private String GHR;
	private StringBuilder newPC;
	private ArrayList<ArrayList<String>> BBS;
	private LRU least_recently_used_BBS;
	private final int BBS_size = 1024;

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

		System.out.println("\n================================================================");
		System.out.println("================================================================");
		System.out.println("================================================================");
		System.out.println("================================================================");
		System.out.println("Predicting branch " + address + " with actual result " + result);
		System.out.println("GHR: " + GHR);
		System.out.println("PC: " + address);
		System.out.println("XOR: " + getXOR(address));
		System.out.println("[PHT & BBS Before]");
		printPHT();
		printBBS();

		for (int i = 0; i < PHT.size(); i++) {
			if (PHT.get(i).get(0).equals(xor)) {
				found = true;
				index = i;
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
			replaceLRUPHT(xor);
		}

		int BBS_index = -1;
		// Check the Biasing Bit Storage.
		found = false;
		for (int i = 0; i < BBS_size; i++) {
			if (BBS.get(i).get(0).equals(address)) {
				found = true;
				BBS_index = i;
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
			updateGHR(result);
			replaceLRUBBS(address, result);
			System.out.println("Predict NONE");
			System.out.println("[PHT & BBS After]");
			printPHT();
			printBBS();
			return Predict.NONE;
		}

		updateGHR(result);

		if (index == -1) {
			System.out.println("Predict NONE");
			System.out.println("[PHT & BBS After]");
			printPHT();
			printBBS();
			return Predict.NONE;
		}

		boolean xnor_result = getXNOR(PHTBit, BBSBit);

		// Increment PHT counter if bits agree,
		// Otherwise decrement.
		if (xnor_result == true) {
			System.out.println("Predict TRUE");
			updatePHTState(index, xor, "T");
			System.out.println("[PHT & BBS After]");
			printPHT();
			printBBS();
			return Predict.TRUE;
		} else {
			System.out.println("Predict FALSE");
			updatePHTState(index, xor, "F");
			System.out.println("[PHT & BBS After]");
			printPHT();
			printBBS();
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

		// System.out.println("Binary " + newPC);

		if (newPC.length() < 64) {
			int add = 64 - newPC.length();
			for (int i = 0; i < add; i++) {
				newPC.insert(0, "0");
			}
		}

		String finalPC = newPC.toString();
		int endLength = newPC.length() - 6;
		// System.out.println("Before trim" + finalPC);
		finalPC = finalPC.substring(endLength - 10, endLength);
		// System.out.println("After trim" + finalPC);
		return String.valueOf((Long.parseLong((Utility.BinToDec(GHR))) ^ Long.parseLong(finalPC)));
	}

	private boolean getXNOR(boolean PHTbit, boolean BTBbit) {
		return !(PHTbit ^ BTBbit);
	}

	private void replaceLRUBBS(String address, String result) {
		String lru = least_recently_used_BBS.getLRU();
		int replace = -1;
		boolean full = true;

		for (int i = 0; i < BBS.size(); i++) {
			if (BBS.get(i).get(0).equals("-1")) {
				replace = i;
				full = false;
				break;
			}
		}
		if (full) {
			for (int i = 0; i < BBS.size(); i++) {
				if (BBS.get(i).get(0).equals(lru)) {
					replace = i;
					break;
				}
			}
		}

		System.out.println("Replacing BBS index " + replace + " with " + address);
		BBS.get(replace).set(0, address);
		if (result.equals("T") || result.equals("1")) {
			BBS.get(replace).set(1, "1");
		} else {
			BBS.get(replace).set(1, "0");
		}
	}

	private void printBBS() {
		System.out.println("-----------Biasing Bit Storage--------");
		for (int i = 0; i < BBS.size(); i++) {
			if (BBS.get(i).get(0).equals("-1")) {
				break;
			}
			System.out.println(BBS.get(i).get(0) + " --> " + BBS.get(i).get(1));
		}
	}
}
