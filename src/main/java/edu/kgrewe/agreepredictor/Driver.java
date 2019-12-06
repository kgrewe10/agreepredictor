package edu.kgrewe.agreepredictor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Driver {
	public static void main(String[] args) {
		// If there a no arguments, throw an error.
		if (args.length == 0) {
			System.out.println("You must enter arguments.  Type in help as a parameter for more info.");
			System.exit(0);
		}

		String type = "";
		long table_size = 0;
		long counter_bits = 0;
		String file_in = "";
		String file_out = "";
		File out = null;
		PrintWriter printer = null;

		try {
			type = args[0];
			table_size = Long.parseLong(args[1]);
			counter_bits = Long.parseLong(args[2]);
			file_in = args[3];
			file_out = args[4];
		} catch (Exception e) {
			System.out.println("Mismatched argument types.\nEnter help as the only argument to view the format.");
			System.exit(0);
		}

		if (!Utility.isPowerOfTwo((int) table_size) || table_size < 0) {
			System.out.println("Table size invalid.");
			System.exit(0);
		}

		if (counter_bits%2 != 0 || counter_bits < 2) {
			System.out.println("Bit counter size invalid.");
			System.exit(0);
		}

		if (args.length == 5 || args.length == 1) {
			if (args[0].equals("help") || args.length == 1) {
				System.out.println("Branch Predictor\n\nHELP MENU");
				System.out.println("This application takes in 5 arguments in order as specified below.");
				System.out.println("\nARGUMENTS FORMAT\n");
				System.out.println("[type] [table_size] [counter_bits] [inputfilename] [outputfilename]\n");
				System.out.println("type: The type of branch predictor.");
				System.out.println(
						"     values: 1L - One Level, 2G - Two Level Global, GS - GShare, 2L - Two Level Local");
				System.out.println("table_size: The total number of entries in the PHT.");
				System.out.println("     values: any multiple of two");
				System.out.println("counter_bits: The number of bits to use for the PHT counter.");
				System.out.println("     values: any multiple of two");
				System.out.println("inputfilename: The name of the input file to use for the simulation.");
				System.out.println("     values: a string");
				System.out.println("outputfilename: The name of the output file to create with results.");
				System.out.println("     values: a string\n");
				System.exit(0);
			}

			out = new File(file_out + ".txt");
			try {
				printer = new PrintWriter(out);
			} catch (FileNotFoundException e1) {
				System.out.println("Couldn't create PrintWriter");
				System.exit(0);
			}

			System.out.println("\n[SETUP]");
			System.out.println("Reading input...");
			// Prints out the values entered to the user.
			System.out.println("Type: " + type);
			System.out.println("Table Size: " + table_size);
			System.out.println("Counter Bits: " + counter_bits);
			System.out.println("Input Filename: " + file_in);
			System.out.println("Output Filename: " + file_out + ".txt");

			printer.println("Type: " + type);
			printer.println("Table Size: " + table_size);
			printer.println("Counter Bits: " + counter_bits);
			printer.println("Input Filename: " + file_in);
			printer.println("Output Filename: " + file_out + ".txt");

			System.out.println("Parsing input file...");
			// Read input file and parse it.
			Scanner input = null;
			ArrayList<String> branch_instructions = new ArrayList<String>();
			int i = 0;
			try {
				input = new Scanner(new File(file_in));
				while (input.hasNext()) {
					String line = input.nextLine();
					//System.out.println(line);
					String arr[] = line.split(" ");
					//System.out.println(arr[0]);
					if (arr[0].contains("0x")) {
						arr[0] = arr[0].replace("0x", "");
						//System.out.println(arr[0]);
						String newArr[] = arr[0].split("\t");
						//System.out.println(newArr[0] + " " + newArr[1]);
						arr[0] = newArr[0];
						arr[1] = newArr[1];
					}
					
//					i++;
//					if (i > 50000000) {
//						break;
//					}
					//System.out.println(arr[0] + " " + arr[1]);
					branch_instructions.add(arr[0] + " " + arr[1]);
				}
				input.close();
			} catch (Exception e) {
				System.out.println("Error reading input file.  Check path and rerun the application.");
				System.exit(0);
			}

			System.out.println("Building branch predictor...");
			BranchPredictorSim bps = null;
			BranchPredictor bp = null;

			// Determine the type of branch predictor to create.
			if (type.equals("1L")) {
				// One Level.
				bp = new OneLevel(table_size, counter_bits);
				System.out.println("Creating one level branch predictor with PHT size " + table_size + "...");
				
			} else if (type.equals("2G")) {
				// Two Level Global.
				bp = new TwoLevelGlobal(table_size, counter_bits);
				System.out.println("Creating two level global branch predictor with PHT size " + table_size + " and 10 bit GHR...");
			} else if (type.equals("GS")) {
				// GShare.
				bp = new GShare(table_size, counter_bits);
				System.out.println("Creating gshare branch predictor with PHT size " + table_size + " and 10 bit GHR...");
			} else if (type.equals("2L")) {
				// Two Level Local.
				bp = new TwoLevelLocal(table_size, counter_bits);
				System.out.println("Creating two level local branch predictor with PHT size " + table_size + " and 10 bit local history...");
			} else {
				System.out.println("Invalid argument for type. Rerun and try again.");
				System.exit(0);
			}
			bps = new BranchPredictorSim(bp);

			System.out.println("Setup complete.");

			// Starts the simulation.
			System.out.println("\n[SIMULATE]");
			System.out.println("Starting branch predictor simulation...");
			String results = bps.simulate(branch_instructions);
			System.out.println("Branch predictor simulation completed.");
			System.out.println(results);

			printer.println(results);
		} else {
			System.out.println("Invalid number of arguments.  Rerun and try again.");
			System.exit(0);
		}
		printer.close();
	}

}
