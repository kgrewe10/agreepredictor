package edu.kgrewe.agreepredictor;

import java.util.ArrayList;

public class OneLevel extends BranchPredictor {
	public OneLevel() {
		super();
	}

	public OneLevel(long table_size, long counter_bits) {
		super(table_size, counter_bits);
	}

	@Override
	public Predict prediction(String address, String result) {
		access(address);
		boolean found = false;
		int index = -1;
		ArrayList<ArrayList<String>> PHT = getPHT();
		// printPHT();
		// printLRU();

		for (int i = 0; i < PHT.size(); i++) {
			if (PHT.get(i).get(0).equals(address)) {
				found = true;
				index = i;
			}
		}

		if (found) {
			String PHT_value = PHT.get(index).get(1);
			// Prediction is on a border state, don't predict.
			if (PHT_value.equals(getMax_value()) || PHT_value.equals(getMin_value())) {
				updatePHTState(index, address, result);
				// System.out.println("No prediction");
				return Predict.NONE;
			}

			// If less than lower border state, predict false.
			// Else predict true.
			if (PHT_value.compareTo(getMin_value()) < 0) {
				updatePHTState(index, address, result);
				// System.out.println("Predict False");
				return Predict.FALSE;
			} else {
				updatePHTState(index, address, result);
				// System.out.println("Predict True");
				return Predict.TRUE;
			}
		} else {
			replaceLRUPHT(address);
		}
		return Predict.NONE;
	}

}
