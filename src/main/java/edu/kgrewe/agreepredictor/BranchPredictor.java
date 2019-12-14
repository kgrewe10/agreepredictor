package edu.kgrewe.agreepredictor;

import java.util.ArrayList;

public abstract class BranchPredictor {
	private final long TABLE_SIZE;
	private final long COUNTER_BITS;
	private final long MAX_COUNTER;
	private ArrayList<ArrayList<String>> PHT;
	private LRU least_recently_used;
	private final String MAX_VALUE;
	private final String MIN_VALUE;

	public BranchPredictor() {
		this.TABLE_SIZE = 1024;
		this.COUNTER_BITS = 2;
		this.MAX_COUNTER = (long) Math.pow(2, COUNTER_BITS) - 1;
		MAX_VALUE = String.valueOf((getMax_counter() + 1) / 2);
		MIN_VALUE = String.valueOf(((getMax_counter() + 1) / 2) - 1);
		PHT = new ArrayList<ArrayList<String>>();
		least_recently_used = new LRU((int) TABLE_SIZE);

		// Initialize PHT.
		for (int i = 0; i < TABLE_SIZE; i++) {
			ArrayList<String> row = new ArrayList<String>();
			row.add(String.valueOf(-1));
			row.add(String.valueOf(0));
			PHT.add(row);
		}
	}

	public BranchPredictor(long table_size, long counter_bits) {
		this.TABLE_SIZE = table_size;
		this.COUNTER_BITS = counter_bits;
		this.MAX_COUNTER = (long) Math.pow(2, counter_bits) - 1;
		MAX_VALUE = String.valueOf((getMax_counter() + 1) / 2);
		MIN_VALUE = String.valueOf(((getMax_counter() + 1) / 2) - 1);
		PHT = new ArrayList<ArrayList<String>>();
		least_recently_used = new LRU((int) table_size);

		// Initialize PHT.
		for (int i = 0; i < table_size; i++) {
			ArrayList<String> row = new ArrayList<String>();
			row.add(String.valueOf(-1));
			row.add(String.valueOf(0));
			PHT.add(row);
		}
	}

	public long getTable_size() {
		return TABLE_SIZE;
	}

	public void access(String address) {
		least_recently_used.access(address);
	}

	public long getCounter_bits() {
		return COUNTER_BITS;
	}

	public abstract Predict prediction(String address, String result);

	public ArrayList<ArrayList<String>> getPHT() {
		return PHT;
	}

	public void setPHT(ArrayList<ArrayList<String>> pHT) {
		PHT = pHT;
	}

	public long getMax_counter() {
		return MAX_COUNTER;
	}

	public void printPHT() {
		System.out.println("-----------PHT--------");
		for (int i = 0; i < PHT.size(); i++) {
			if (PHT.get(i).get(0).equals("-1")) {
				break;
			}
			System.out.println(PHT.get(i).get(0) + " --> " + PHT.get(i).get(1));
		}
	}

	public void printLRU() {
		least_recently_used.display();
	}

	public void updatePHTState(int index, String address, String result) {
		ArrayList<ArrayList<String>> new_hash = getPHT();
		String val = new_hash.get(index).get(1);
		int value = Integer.parseInt(val);
		if ((result.equals("T") || result.equals("1")) && (value < MAX_COUNTER)) {
			// System.out.println("True");
			new_hash.get(index).set(1, String.valueOf((value) + 1));
		} else if ((result.equals("N") || result.equals("0")) && (value > 0)) {
			// System.out.println("False");
			new_hash.get(index).set(1, String.valueOf((value) - 1));
		}

		setPHT(new_hash);
	}

	public int replaceLRUPHT(String address) {
		int replace = -1;
		boolean full = true;

		for (int i = 0; i < PHT.size(); i++) {
			if (PHT.get(i).get(0).equals("-1")) {
				replace = i;
				full = false;
				break;
			}
		}
		if (full) {
			// System.out.println("infull");
			String lru = least_recently_used.getLRU();
			//System.out.println("PHT LRU " + lru);
			for (int i = 0; i < PHT.size(); i++) {
				if (PHT.get(i).get(0).equals(lru)) {
					replace = i;
					break;
				}
			}
		}

		//System.out.println("Replacing PHT index " + replace + " with " + address);
		PHT.get(replace).set(0, address);
		PHT.get(replace).set(1, "0");
		return replace;
	}

	public String getMax_value() {
		return MAX_VALUE;
	}

	public String getMin_value() {
		return MIN_VALUE;
	}
}
