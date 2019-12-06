package edu.kgrewe.agreepredictor;

import java.util.ArrayList;

public class TwoLevelLocal extends BranchPredictor {
	private ArrayList<ArrayList<String>> LHT;
	private LRU least_recently_used_LHT;

	public TwoLevelLocal() {
		super();
		LHT = new ArrayList<ArrayList<String>>();
		least_recently_used_LHT = new LRU(128);

		// Initialize LHT.
		for (int i = 0; i < 128; i++) {
			ArrayList<String> row = new ArrayList<String>();
			row.add("-1");
			row.add("-1");
			LHT.add(row);
		}
	}

	public TwoLevelLocal(long table_size, long counter_bits) {
		super(table_size, counter_bits);
		LHT = new ArrayList<ArrayList<String>>();
		least_recently_used_LHT = new LRU(128);

		// Initialize LHT.
		for (int i = 0; i < 128; i++) {
			ArrayList<String> row = new ArrayList<String>();
			row.add("-1");
			row.add("-1");
			LHT.add(row);
		}
	}

	@Override
	public Predict prediction(String address, String result) {
		least_recently_used_LHT.access(address);
		boolean foundLHT = false;
		boolean foundPHT = false;
		int indexLHT = 0;
		int indexPHT = 0;
		ArrayList<ArrayList<String>> PHT = getPHT();
		// printLHT();
		// printPHT();
		// Try to find PC in local history table.
		String localValue = "";
		for (int i = 0; i < LHT.size(); i++) {
			if (LHT.get(i).get(0).equals(address)) {
				foundLHT = true;
				indexLHT = i;
				localValue = LHT.get(indexLHT).get(1);
				access(localValue);
			}
		}

		// If found in local history, find in pattern history.
		if (foundLHT) {
			for (int i = 0; i < PHT.size(); i++) {
				if (PHT.get(i).get(0).equals(localValue)) {
					updateLocalHistory(indexLHT, localValue, result);
					foundPHT = true;
					indexPHT = i;
					// System.out.println("PHT index found " + indexPHT);
				}
			}
		} else {
			replaceLRULHT(address);
			return Predict.NONE;
		}

		String PHT_value = PHT.get(indexPHT).get(1);
		if (foundPHT) {
			// Prediction is on a border state, don't predict.
			if (PHT_value.equals(getMax_value())
					|| PHT_value.equals(getMin_value())) {
				updatePHTState(indexPHT, localValue, result);
				return Predict.NONE;
			}

			// If less than lower border state, predict false.
			// Else predict true.
			if (PHT_value.compareTo(getMin_value()) < 0) {
				updatePHTState(indexPHT, localValue, result);
				return Predict.FALSE;
			} else {
				updatePHTState(indexPHT, localValue, result);
				return Predict.TRUE;
			}
		} else {
			replaceLRUPHT(localValue);
		}
		return Predict.NONE;
	}

	public void replaceLRULHT(String address) {
		String lru = least_recently_used_LHT.getLRU();
		int replace = -1;
		boolean full = true;

		for (int i = 0; i < LHT.size(); i++) {
			if (LHT.get(i).get(0).equals("-1")) {
				replace = least_recently_used_LHT.getSize() - 1;
				full = false;
			}
		}
		if (full) {
			for (int i = 0; i < LHT.size(); i++) {
				if (LHT.get(i).get(0).equals(lru)) {
					replace = i - 1;
				}
			}
		}

		if (replace == -1) {
			replace = least_recently_used_LHT.getSize() - 1;
		}

		// System.out.println("Replacing LHT index " + replace + " with " + address);
		LHT.get(replace).set(0, address);
		LHT.get(replace).set(1, "0");

	}

	private void updateLocalHistory(int index, String value, String result) {
		// If GHR is 10 bits, remove the oldest bit.
		if (value.length() == 10) {
			// System.out.println("Before shift " + GHR);
			value = value.substring(1, value.length());
			// System.out.println("After shift " + GHR);
		}

		if (result.equals("T") || result.equals("1")) {
			value += "1";
		} else {
			value += "0";
		}

		LHT.get(index).set(1, value);
	}

	private void printLHT() {
		System.out.println("-----------LocalHistoryTable--------");
		for (int i = 0; i < LHT.size(); i++) {
			if (LHT.get(i).get(0).equals("-1")) {
				break;
			}
			System.out.println(LHT.get(i).get(0) + " --> " + LHT.get(i).get(1));
		}
	}
}
