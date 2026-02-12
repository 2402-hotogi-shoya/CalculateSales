package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String FILE_NOT_SEQUENCE ="売上ファイル名が連番になっていません";
	private static final String AMOUNT_OVER_10_DIGITS ="合計金額が10桁を超えました";
	//ファイル名 + AMOUNT_CODE_ERROR
	private static final String AMOUNT_CODE_ERROR ="の支店コードが不正です";
	//ファイル名 + AMOUNT_FORMAT_ERROR
	private static final String AMOUNT_FORMAT_ERROR ="のフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {

		//ERROR
		//コマンドライン引数が1つ
		if (args.length != 1) {
		    //コマンドライン引数が1つ設定されていなかった場合は、
		    //エラーメッセージをコンソールに表⽰します。
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//listFilesを使⽤してfilesという配列に、
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納します。
		File[] files = new File(args[0]).listFiles();

		//ファイルの情報を格納する List(ArrayList)
		List<File> rcdFiles = new ArrayList<File>();

		for(int i = 0; i < files.length ; i++) {
			//ファイル名が取得
			//matches を使用してファイル名が「数字8桁.rcd」なのか判定。
			if(files[i].isFile() && files[i].getName() .matches("^[0-9]{8}\\.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}

		//ERROR
		//ファイル名が連番
		//⽐較回数は売上ファイルの数よりも1回少ないため、
		//繰り返し回数は売上ファイルのリストの数よりも1つ⼩さい数です。
		Collections.sort(rcdFiles);
		for(int i = 0; i < rcdFiles.size() -1; i++) {
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			//⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し、int型に変換します。
			if((latter - former) != 1) {
				//2つのファイル名の数字を⽐較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表⽰します。
				System.out.println(FILE_NOT_SEQUENCE);
				return;
			}
		}

		for(int i = 0; i < rcdFiles.size(); i++) {

			BufferedReader br = null;

			List<String> fileLine = new ArrayList<>();
			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。
			try {
				FileReader fr = new FileReader(rcdFiles.get(i));
				br = new BufferedReader(fr);

				String line;
				// 一行ずつ読み込む
				while((line = br.readLine()) != null) {
					//1行目=支店コード、2行目=金額
					fileLine.add(line);
				}
			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}


			//ERROR
			//支店コードが支店情報にない
			if (!branchSales.containsKey(fileLine.get(0))) {
				//⽀店情報を保持しているMapに売上ファイルの⽀店コードが存在しなかった場合は、
				//エラーメッセージをコンソールに表⽰します。
				//ファイル名 + の⽀店コードが不正です
				System.out.println(rcdFiles.get(i).getName() + AMOUNT_CODE_ERROR);
				return;
			}

			//ERROR
			//売上ファイルの中身が2行ではない
			if(fileLine.size() != 2) {
			    //売上ファイルの⾏数が2⾏ではなかった場合は、
			    //エラーメッセージをコンソールに表⽰します。
				System.out.println(rcdFiles.get(i).getName() + AMOUNT_FORMAT_ERROR);
				return;
			}

			//ERROR
			//取得した金額が数字か
			if(!fileLine.get(1).matches("^[0-9]+$")) {
			    //売上⾦額が数字ではなかった場合は、
			    //エラーメッセージをコンソールに表⽰します。
				System.out.println(UNKNOWN_ERROR);
				return;
			}


			//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
			//※詳細は後述で説明
			long fileSale = Long.parseLong(fileLine.get(1));
			//読み込んだ売上⾦額を加算します。
			//※詳細は後述で説明
			Long saleAmount = branchSales.get(fileLine.get(0)) + fileSale;

			//ERROR
			//金額が11桁以上(最大10)
			if(saleAmount >= 10000000000L){
				System.out.println(AMOUNT_OVER_10_DIGITS);
				return;
			}


			//加算した売上⾦額をMapに追加します。
			branchSales.put(fileLine.get(0), saleAmount);
		}


		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);


			//ERROR
			//支店定義ファイルのあること
			if(!file.exists()) {
			    //⽀店定義ファイルが存在しない場合、コンソールにエラーメッセージを表⽰します。
				System.out.println(FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む

			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				System.out.println(line);
				//⽀店定義ファイル格納
				String[] items = line.split(",");


				//ERROR
				//支店ファイルの数字が3桁
				//ファイルのカラムが2つであること
			    if((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))){
			    	//⽀店定義ファイルの仕様が満たされていない場合、
			    	//エラーメッセージをコンソールに表⽰します。
			    	System.out.println(FILE_INVALID_FORMAT);
					return false;
			    }

			    branchNames.put(items[0], items[1]);
			    branchSales.put(items[0], 0L);
			}


		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;
		try {
			File file = new File(path, fileName);
			FileWriter fr = new FileWriter(file);
			bw = new BufferedWriter(fr);
			for (String key : branchNames.keySet()) {
				//keyという変数には、Mapから取得したキーが代入されています。
				//拡張for⽂で繰り返されているので、1つ⽬のキーが取得できたら、
				//2つ⽬の取得...といったように、次々とkeyという変数に上書きされていきます。
				String text = key + "," + branchNames.get(key) + "," + branchSales.get(key);
				bw.write(text);
				bw.newLine();
			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}
