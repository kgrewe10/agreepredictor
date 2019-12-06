package edu.kgrewe.agreepredictor;

import java.util.ArrayList;

public class TwoLevelGlobal extends BranchPredictor {
	private String GHR;

	public TwoLevelGlobal() {
		super();
		GHR = "0";
	}

	public TwoLevelGlobal(long table_size, long counter_bits) {
		super(table_size, counter_bits);
		GHR = "0";
	}

	@Override
	public Predict prediction(String address, String result) {
		//System.out.println("New branch: " + address + " with result " + result);
		access(GHR);
		boolean found = false;
		int index = -1;
		ArrayList<ArrayList<String>> PHT = getPHT();
		// System.out.println("PHT BEfore");
		// printPHT();
		for (int i = 0; i < PHT.size(); i++) {
			if (PHT.get(i).get(0).equals(GHR)) {
				found = true;
				index = i;
			}
		}

		if (found) {
			String PHT_value = PHT.get(index).get(1);
			// Prediction is on a border state, don't predict.
			if (PHT_value.equals(getMax_value())
					|| PHT_value.equals(getMin_value())) {
				updatePHTState(index, GHR, result);
				updateGHR(result);
				// System.out.println("PHT After");
				// printPHT();
				return Predict.NONE;
			}

			// If less than lower border state, predict false.
			// Else predict true.
			if (PHT_value.compareTo(getMin_value()) < 0) {
				updatePHTState(index, GHR, result);
				updateGHR(result);
				// System.out.println("PHT After");
				// printPHT();
				return Predict.FALSE;
			} else {
				updatePHTState(index, GHR, result);
				updateGHR(result);
				// System.out.println("PHT After");
				// printPHT();
				return Predict.TRUE;
			}
		} else {
			replaceLRUPHT(GHR);
		}
		updateGHR(result);
		// System.out.println("PHT After");
		// printPHT();
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
			//System.out.println("update true");
			GHR += "1";
		} else {
			//System.out.println("update false");
			GHR += "0";
		}
	}
}
