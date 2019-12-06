package edu.kgrewe.agreepredictor;

import java.text.DecimalFormat;
import java.util.List;

public class BranchPredictorSim {
	private final BranchPredictor bp;
	private long predictions;
	private long nonpredictions;
	private long misses;
	private final DecimalFormat df4 = new DecimalFormat("#.####");

	public BranchPredictorSim(BranchPredictor bp) {
		this.bp = bp;
		this.predictions = 0;
		this.nonpredictions = 0;
		this.misses = 0;
	}

	public String simulate(List<String> branches) {
		for (int i = 0; i < branches.size(); i++) {
			String arr[] = branches.get(i).split(" ");
			Predict result = bp.prediction(arr[0], arr[1]);
			switch (result) {
			case NONE:
				nonpredictions++;
				break;
			case FALSE:
				predictions++;
				if (arr[1].equals("T") || arr[1].equals("1")) {
					misses++;
				}
				break;
			case TRUE:
				predictions++;
				if (arr[1].equals("N") || arr[1].equals("0")) {
					misses++;
				}
				break;
			default:
				System.out.println("Prediction not found.");
			}
		}

		double miss = misses;
		return "\n[Branch Prediction Results]\nTotal Predictions: " + predictions + "\nTotal Non-Predictions: "
				+ nonpredictions + "\nTotal Misses: " + misses + "\nMiss %: "
				+ df4.format((miss / (predictions + nonpredictions)) * 100);
	}
}
