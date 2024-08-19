import java.util.Scanner;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Hotel{
	
	private static String url = "jdbc:mysql://localhost:3306/hotel";
	private static String username = "root";
	private static String password = "sricheechu_2005";
	public static void main(String args[]) throws Exception{
		while(true) {
			Connection con = DriverManager.getConnection(url,username,password);
			System.out.println();
			Scanner sc = new Scanner(System.in);
			System.out.println("HOTEL RESERVATION SYSTEM");
			System.out.println("1.Reserve a room");
			System.out.println("2.View Reservations");
			System.out.println("3.Get Room Number");
			System.out.println("4.Update Reservation ");
			System.out.println("5.Delete Reservations");
			System.out.println("6.Download PDF");
			System.out.println("0.Exit");
			System.out.println("Enter your choice: ");
			int choice = sc.nextInt();
			switch(choice) {
			case 1:
				reserveRoom(con,sc);
				break;
			
			case 2:
				viewReservations(con);
				break;
				
			case 3:
				getRoomNumber(con,sc);
				break;
				
			case 4:
				updateReservation(con,sc);
				break;
			
			case 5:
				deleteReservation(con,sc);
				break;
		    
			case 6:
				downloadPDF(con,sc);
				break;
			case 0:
				exit();
				sc.close();
				return;
				
			default:
				System.out.println("Invalid choice..! Please try again..!");
			}
		}
	}
	private static void reserveRoom(Connection  con, Scanner sc) throws SQLException {
			System.out.print("Enter the reservation id sent via mail: ");
			int reservationID = sc.nextInt();
			System.out.print("Enter the Guest Name: ");
			String guest = sc.next();
			sc.nextLine();
			System.out.print("Enter the Room Number: ");
			int room = sc.nextInt();
			sc.nextLine();
			System.out.println("Enter the Contact Number: ");
			String contact_num = sc.nextLine();
			System.out.println("Enter the reservation Date(YYYY-MM-DD)");
			String date = sc.nextLine();
			String query = "INSERT INTO RESERVATIONS VALUES(?,?,?,?,?)";
			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1,reservationID);
			pst.setString(2, guest);
			pst.setInt(3,room);
			pst.setString(4,contact_num);
			pst.setString(5,date);
			int Rows = pst.executeUpdate();
			if(Rows > 0) {
				System.out.println("Reservation Successful");
			}else {
				System.out.println("Reservation failed..! Please try again..");
			}
	}
	
	private static void viewReservations(Connection con) throws SQLException{
		String query = "SELECT RESERVATIONID,GUEST,ROOMNUMBER,CONTACTNUMBER,RESERVATIONDATE FROM RESERVATIONS";
		
		try(Statement statement = con.createStatement()){
			ResultSet resultset = statement.executeQuery(query);
			System.out.println("Current Reservations");
			System.out.println("+----------------+-------------+-------------+----------------+------------------+");
			System.out.println("| Reservation ID |  Guest Name | Room Number | Contact Number | Reservation Date |");
			System.out.println("+----------------+-------------+-------------+----------------+------------------+");
			while(resultset.next()) {
                int reservationId = resultset.getInt("reservationid");
                String guestName = resultset.getString("guest");
                int roomNumber = resultset.getInt("roomnumber");
                String contactNumber = resultset.getString("contactnumber");
                String reservationDate = resultset.getTimestamp("reservationdate").toString();
                System.out.printf("|     %4d       |     %s    |       %d     |      %s       |        %s    |\n",reservationId,guestName,roomNumber,contactNumber,reservationDate);
             
			}
			System.out.println("+----------------+-------------+-------------+----------------+------------------+");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	private static void getRoomNumber(Connection con,Scanner sc) throws SQLException{
		System.out.println("Enter your Reservation ID to view your Room Number: ");
		int ID = sc.nextInt();
		System.out.println("Enter the guest name: ");
		sc.nextLine();
		String guest = sc.nextLine();
        String sql = "SELECT ROOMNUMBER FROM RESERVATIONS " +
                "WHERE RESERVATIONID = " + ID +
                " AND GUEST = '" + guest + "'";
        try (Statement statement = con.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                int roomNumber = resultSet.getInt("ROOMNUMBER");
                System.out.println("Room number for Reservation ID " + ID +
                        " and Guest " + guest + " is: " + roomNumber);
            } else {
                System.out.println("Reservation not found for the given ID and guest name.");
            }
        }
	}
	private static void updateReservation(Connection con,Scanner sc) throws SQLException{
		System.out.println("Enter the reservation ID to update: ");
		int ID = sc.nextInt();
		sc.nextLine();
		if(!reservationExists(con,ID)) {
			return;
		}
		System.out.print("Enter the new guest name: ");
		String new_guest = sc.nextLine();
//		sc.nextLine();
		System.out.println("Enter the new room number: ");
		int room_no = sc.nextInt();
		System.out.print("Enter the contact number: ");
		String contact = sc.next();
		String query = "UPDATE RESERVATIONS SET GUEST = ?, ROOMNUMBER = ?,CONTACTNUMBER = ? WHERE RESERVATIONID = ?";
		con.setAutoCommit(false);
		PreparedStatement pst = con.prepareStatement(query);
		pst.setString(1, new_guest);
		pst.setInt(2, room_no);
		pst.setString(3, contact);
		pst.setInt(4, ID);
		int rows = pst.executeUpdate();
		con.commit();
		if(rows > 0) {
			System.out.println("Reservation updated Successfully");
		}else {
			System.out.println("Reservation update failed");
		}
	}
	private static void deleteReservation(Connection con, Scanner sc) throws SQLException {
		System.out.println("Enter the reservation ID to delete: ");
		int ID = sc.nextInt();
		if(!reservationExists(con,ID)) {
			System.out.println("Reservation not found for the Given ID");
			return;
		}
		String query = "DELETE FROM RESERVATIONS WHERE RESERVATIONID = " + ID;
		Statement st = con.createStatement();
		int rows = st.executeUpdate(query);
		if(rows > 0) {
			System.out.println("Reservation deleted successfully");
		}else {
			System.out.println("Reservation deletion failed..!");
		}
	}
    private static boolean reservationExists(Connection con, int reservationId) {
        try {
            String sql = "SELECT RESERVATIONID FROM RESERVATIONS WHERE RESERVATIONID = " + reservationId;

            try (Statement statement = con.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                return resultSet.next(); // If there's a result, the reservation exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  
        }
    }
    
    private static void downloadPDF(Connection con, Scanner sc) throws DocumentException, SQLException {
    	//String laptopBrand = sc.nextLine();
    	sc.nextLine();
    	final String PDF_FINALPATH = "C:\\Users\\Lenovo\\";
		System.out.println("Create PDF using iText");
		Document document1 = new Document();
		System.out.println("Enter the reservation ID: ");
		int reservationID = sc.nextInt();
		if(!reservationExists(con,reservationID)) {
			System.out.println("Reservation ID not found..!");
			return;
		}
    	final String PDF_NAME = reservationID+".pdf";
		String query = "SELECT RESERVATIONID,GUEST,ROOMNUMBER,CONTACTNUMBER,RESERVATIONDATE FROM RESERVATIONS WHERE RESERVATIONID = " + reservationID;
        File pdfFile = new File(PDF_FINALPATH + PDF_NAME);
        try {
            PdfWriter.getInstance(document1, new FileOutputStream(pdfFile));
            document1.open();
            
            Font font = FontFactory.getFont(FontFactory.COURIER_BOLD, 14, com.itextpdf.text.BaseColor.BLACK);
            
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            
            if (resultSet.next()) {
                int reservationId = resultSet.getInt("RESERVATIONID");
                String guestName = resultSet.getString("GUEST");
                int roomNumber = resultSet.getInt("ROOMNUMBER");
                String contactNumber = resultSet.getString("CONTACTNUMBER");
                String reservationDate = resultSet.getString("RESERVATIONDATE");

                document1.add(new Paragraph("Reservation ID: " + reservationId, font));
                document1.add(new Paragraph("Guest Name: " + guestName, font));
                document1.add(new Paragraph("Room Number: " + roomNumber, font));
                document1.add(new Paragraph("Contact Number: " + contactNumber, font));
                document1.add(new Paragraph("Reservation Date: " + reservationDate, font));
            } else {
                System.out.println("No data found for the given reservation ID.");
            }
            
            document1.add(new Paragraph("\n",font));
            document1.add(new Paragraph("Thank you booking the Hotel.. Have a great day..!",font));
            document1.close();
            System.out.println("PDF created successfully");
            if(Desktop.isDesktopSupported()) {
            	Desktop desktop = Desktop.getDesktop();
            	if(pdfFile.exists()) {
            		desktop.open(pdfFile);
            	}else {
            		System.out.println("File does not exist");
            	}
            }else {
            	System.out.println("Desktop is not supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            con.close();
        }
    }

    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 5;
        while(i!=0){
            System.out.print(".");
            Thread.sleep(1000);
            i--;
        }
        System.out.println();
        System.out.println("ThankYou For Using Hotel Reservation System!!!");
    }
}
