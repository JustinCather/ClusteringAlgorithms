package excel;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ImportExcel {

	private ImportExcel() {
	}

	public static void test() throws BiffException, IOException {
		Workbook workbook = Workbook.getWorkbook(new File("E:\\temp\\ClusteringAlgorithms\\ClusteringAlgorithms\\data\\Iris Data Set.xls"));
		Sheet sheet = workbook.getSheet(0);
		
		Cell cell1 = sheet.getCell(0, 2);
		System.out.println(cell1.getContents());
		Cell cell2 = sheet.getCell(3, 4);
		System.out.println(cell2.getContents());
		workbook.close();
	}
	
	public static void main(String[] args){
		try {
			test();
		} catch (BiffException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
