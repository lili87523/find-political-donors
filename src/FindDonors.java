import java.io.*;
import java.util.*;

public class FindDonors{
	public static void main(String[] args){
		if(args.length != 3){
			System.out.println("One input file and Two output files needed!");
			return;
		}

		//create a temp file to store the initial infos needed
		String[] paths = args[0].split("/");
		String filteredInfo = "";

		for(int i = 0; i < paths.length - 1; i++){
				filteredInfo += paths[i] + "/";
		}

		filteredInfo += "filteredInfo.txt";

		//If the output files already exist, delete them first
		deleteFiles(args[1]);
		deleteFiles(args[2]);
		deleteFiles(filteredInfo);

		//Read in input file & write in filtedInfo file
		writeFilteredFile(args[0], filteredInfo);
		
		//Read in filtedInfo file and write in medianvals_by_zip
		writeMedianvals_by_zip(filteredInfo, args[1]);
		
		//Read in filtedInfo file and write in medianvals_by_date
		writeMedianvals_by_date(filteredInfo, args[2]);
	}

	private static void deleteFiles(String input){
		File file = new File(input);
		if(file.isFile()){
			file.delete();
		}
	}

	private static void writeFilteredFile(String input, String output){
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;

		//Read in input file & write in filtedInfo file
		try{
			bufferedReader = new BufferedReader(new FileReader(input));
			bufferedWriter = new BufferedWriter(new FileWriter(output, true));

			String line = null;
			while((line = bufferedReader.readLine()) != null){
				String[] temp = line.split("\\|");
				String[] record = new String[4];

				//Consideration 5
				if(temp[0].length() == 0 || temp[14].length() == 0){
					continue;
				}

				//Consideration 1
				if(temp[15].length() == 0){
					record[0] = temp[0];
					record[1] = temp[10];
					record[2] = temp[13];
					record[3] = temp[14];

					bufferedWriter.write(record[0] + "|" + record[1] + "|"
						+ record[2] + "|" + record[3] + "\n");
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				bufferedReader.close();
				if(bufferedWriter != null){
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	private static void writeMedianvals_by_zip(String input, String output){
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;

		try{
			bufferedReader = new BufferedReader(new FileReader(input));
			bufferedWriter = new BufferedWriter(new FileWriter(output, true));

			HashMap<String[], ArrayList<Integer>>  map_by_zip = new HashMap<String[], ArrayList<Integer>>();

			String line = null;
			//Group records according to key[CMTE_ID, Zip]
			while((line = bufferedReader.readLine()) != null){
				String[] record = line.split("\\|");
			
				//Consideration 3 & 4
				if(record[1].length() >= 5){
					record[1] = record[1].substring(0, 5);
					String[] key = new String[2];
					key[0] = record[0];
					key[1] = record[1];

					//update map_by_zip
					ArrayList<Integer> value = new ArrayList<Integer>();
					for(String[] tempKey : map_by_zip.keySet()){
						if(Arrays.equals(tempKey, key)){
							key = tempKey;
							value = map_by_zip.get(tempKey);
						}
					}
					value.add(Integer.parseInt(record[3]));
					map_by_zip.put(key, value);


					String[] record_by_zip = new String[5];

					//CMTE_ID
					record_by_zip[0] = record[0];
					//ZIP
					record_by_zip[1] = record[1];

					//Sort the amount
					Collections.sort(map_by_zip.get(key));
					int size = map_by_zip.get(key).size();

					//Calculate the median contribution
					int median = 0;
					if(size % 2 != 0){
						median = map_by_zip.get(key).get(size / 2);
					}else{
						median = (int)((map_by_zip.get(key).get(size / 2) + map_by_zip.get(key).get(size / 2 - 1)) / 2.0 + 0.5);
					}
					record_by_zip[2] = String.valueOf(median);

					//Total number of transactions
					record_by_zip[3] = String.valueOf(size);

					//Total amount of contributions
					int sum = 0;
					for(int amount : map_by_zip.get(key)){
						sum += amount;
					}
					record_by_zip[4] = String.valueOf(sum);

					bufferedWriter.write(record_by_zip[0] + "|" + record_by_zip[1] + "|"
						+ record_by_zip[2] + "|" + record_by_zip[3] + "|" + record_by_zip[4] + "\n");
				}
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				bufferedReader.close();
				if(bufferedWriter != null){
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	private static void writeMedianvals_by_date(String input, String output){
		
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;

		try{
			bufferedReader = new BufferedReader(new FileReader(input));
			bufferedWriter = new BufferedWriter(new FileWriter(output, true));
			ArrayList<String[]> records_by_date = new ArrayList<String[]>();
			HashMap<String[], ArrayList<Integer>>  map_by_date = new HashMap<String[], ArrayList<Integer>>();

			String line = null;
			//Group records according to key[CMTE_ID, Date]
			while((line = bufferedReader.readLine()) != null){
				String[] record = line.split("\\|");

				//Consideration 2
				if(isValidDate(record[2])){
					String[] key = new String[2];
					key[0] = record[0];
					//switch positions of [year and (month + day)], so that can be sorted by chronologically 
					key[1] = record[2].substring(4) + record[2].substring(0, 4);

					//update map_by_date
					ArrayList<Integer> value = new ArrayList<Integer>();
					for(String[] tempKey : map_by_date.keySet()){
						if(Arrays.equals(tempKey, key)){
							key = tempKey;
							value = map_by_date.get(tempKey);
						}
					}
					value.add(Integer.parseInt(record[3]));
					map_by_date.put(key, value);
				}
			}
			//Store medianvals_by_date record for sorting
			for(String[] key : map_by_date.keySet()){
				String[] record_by_date = new String[5];
				record_by_date[0] = key[0];
				record_by_date[1] = key[1];

				//Calculate the median contribution
				int median = 0;
				int size = map_by_date.get(key).size();
				Collections.sort(map_by_date.get(key));

				if(size % 2 != 0){
					median = map_by_date.get(key).get(size / 2);
				}else{
					median = (int)((map_by_date.get(key).get(size / 2) + map_by_date.get(key).get(size / 2 - 1)) / 2.0 + 0.5);
				}
				record_by_date[2] = String.valueOf(median);

				//Total number of transactions
				record_by_date[3] = String.valueOf(size);

				//Total amount of contributions
				int sum = 0;
				for(int amount : map_by_date.get(key)){
					sum += amount;
				}
				record_by_date[4] = String.valueOf(sum);

				records_by_date.add(record_by_date);
			}

			//Sort records_by_date
			Collections.sort(records_by_date, (a, b) -> a[0].equals(b[0]) ? a[1].compareTo(b[1]) : a[0].compareTo(b[0]));

			//Write medianvals_by_date.txt
			for(String[] record : records_by_date){
				bufferedWriter.write(record[0] + "|" + record[1].substring(4) + record[1].substring(0, 4) + "|"
						+ record[2] + "|" + record[3] + "|"
					    + record[4] + "\n");
			}

		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				bufferedReader.close();
				if(bufferedWriter != null){
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	private static boolean isValidDate(String date){
		if(date.length() != 8)
			return false;

		int month = Integer.parseInt(date.substring(0, 2));
		int day = Integer.parseInt(date.substring(2, 4));
		int year = Integer.parseInt(date.substring(4));

		if(month <= 0 || day <= 0 || year <= 0 || month > 12 || year > 2017
			//validate the day
			|| ((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day > 31)
			|| ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30)
			|| (month == 2 && (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) && day > 29)
			|| (month == 2 && !(year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) && day > 28))
			return false;

		return true;
	}

}
