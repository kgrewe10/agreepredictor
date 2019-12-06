package edu.kgrewe.agreepredictor;

import java.util.ArrayList;

public class GShare extends BranchPredictor {
	private String GHR;
	private StringBuilder newPC;

	public GShare() {
		super();
		GHR = "0";
		newPC = null;
	}

	public GShare(long table_size, long counter_bits) {
		super(table_size, counter_bits);
		GHR = "0";
		newPC = null;
	}

	@Override
	public Predict prediction(String address, String result) {
		String xor = getXOR(address);
		access(xor);
		boolean found = false;
		int index = -1;
		ArrayList<ArrayList<String>> PHT = getPHT();

		// System.out.println("\n\nGHR: " + GHR);
		// System.out.println("PC: " + address);
		// System.out.println("XOR: " + getXOR(address));
		// printPHT();
		for (int i = 0; i < PHT.size(); i++) {
			if (PHT.get(i).get(0).equals(xor)) {
				found = true;
				index = i;
			}
		}

		if (found) {
			String PHT_value = PHT.get(index).get(1);
			// Prediction is on a border state, don't predict.
			if (PHT_value.equals(getMax_value())
					|| PHT_value.equals(getMin_value())) {
				updatePHTState(index, xor, result);
				updateGHR(result);
				return Predict.NONE;
			}

			// If less than lower border state, predict false.
			// Else predict true.
			if (PHT_value.compareTo(getMin_value()) < 0) {
				updatePHTState(index, xor, result);
				updateGHR(result);
				return Predict.FALSE;
			} else {
				updatePHTState(index, xor, result);
				updateGHR(result);
				return Predict.TRUE;
			}
		} else {
			replaceLRUPHT(xor);
		}
		updateGHR(result);
		return Predict.NONE;
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

}
