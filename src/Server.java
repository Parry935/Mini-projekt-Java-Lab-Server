
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.*;
import java.util.concurrent.Semaphore;

public class Server
{
    public static void main(String[] args) throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(9999);
        Semaphore semaphore = new Semaphore(1);
        DBContext db = new DBContext();
        Connection connection = db.getContection();
        Socket socket = null;


        while (true)
        {
            try
            {
                socket = serverSocket.accept();

                System.out.println("New query : " + socket);

                Thread t = new ClientHandler(socket,semaphore,connection);
                t.start();

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

class ClientHandler extends Thread {
    final Socket socket;
    final Semaphore semaphore;
    final Connection connection;

    public ClientHandler(Socket socket, Semaphore semaphore, Connection connection) {
        this.socket = socket;
        this.semaphore = semaphore;
        this.connection = connection;
    }

    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            line = in.readLine();


            if (line == null)
                socket.close();
            else {
                String[] operation = line.split(" ", 2);

                String[] data;

                switch (operation[0]) {
                    case "login":
                        data = operation[1].split(" ", 2);
                        out.println(checkUserInDB(data[0], data[1]));
                        break;

                    case "getMovies":
                        out.println(getMovies());
                        break;

                    case "getReservations":
                        out.println(getReservations());
                        break;

                    case "getUsers":
                        out.println(getUsers());
                        break;

                    case "getMoviesWithId":
                        out.println(getMoviesWithId());
                        break;

                    case "checkUserInDbByEmail":
                            out.println(checkUserInDbByEmail(operation[1]));
                        break;

                    case "deleteMovieById":
                        out.println(deleteMovieById(operation[1]));
                        break;

                    case "deleteReservation":
                        data = operation[1].split("#", 3);
                        out.println(deleteReservation(data[0], data[1],data[2]));
                        break;

                    case "updateReservation":
                        data = operation[1].split("#", 3);
                        out.println(updateReservation(data[0], data[1],data[2]));
                        break;

                    case "addUserToDB":
                        data = operation[1].split(" ", 6);
                        out.println(addUserToDB(data[0], data[1],data[2], data[3],data[4], data[5]));
                        break;

                    case "addMovieToDB":
                        data = operation[1].split(" ", 3);
                        String convertDateMovie = data[0].replace("#", " ");
                        out.println(addMovieToDB(convertDateMovie, data[1],data[2]));
                        break;

                    case "getPlaceForMovie":
                        data = operation[1].split("#", 3);
                        out.println(getPlaceForMovie(data[0], data[1],data[2]));
                        break;

                    case "getMovieId":
                        data = operation[1].split("#", 3);
                        out.println(getMovieId(data[0], data[1],data[2]));
                        break;

                    case "getReservationForUser":
                        out.println(getReservationForUser(operation[1]));
                        break;

                    case "addReservationToDb":
                        data = operation[1].split("#", 3);
                        out.println(addReservationToDb(data[0], data[1],data[2]));
                        break;

                }
            }
            semaphore.release();
            out.flush();
            socket.close();

            return;

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            return;
        }

    }

    private String deleteReservation(String idMovie, String idUser, String place) throws SQLException {

        String sql = "DELETE FROM mydatabase.reservation WHERE id_movie= ? and id_user= ? and place= ?";

        PreparedStatement preparedStmt = connection.prepareStatement(sql);

        preparedStmt.setString(1, idMovie);
        preparedStmt.setString(2, idUser);
        preparedStmt.setString(3, place);

        preparedStmt.execute();

        return "Succses";
    }

    private String updateReservation(String idMovie, String idUser, String place) throws SQLException {

        String sql = "UPDATE mydatabase.reservation SET confirm = 1 WHERE id_movie= ? and id_user= ? and place= ?";

        PreparedStatement preparedStmt = connection.prepareStatement(sql);

        preparedStmt.setString(1, idMovie);
        preparedStmt.setString(2, idUser);
        preparedStmt.setString(3, place);

        preparedStmt.execute();

        return "Succses";
    }

    private String addReservationToDb(String idUser, String idMovie, String seats) throws SQLException {

        String sql = "INSERT INTO mydatabase.reservation (id_user, id_movie, place, confirm)"
                + "VALUES (?, ?, ?, ?)";

        PreparedStatement preparedStmt = connection.prepareStatement(sql);

        preparedStmt.setString(1, idUser);
        preparedStmt.setString(2, idMovie);
        preparedStmt.setString(3, seats);
        preparedStmt.setString(4, "0");

        preparedStmt.execute();

        return "Succses";
    }

    private String getPlaceForMovie(String date, String title, String type) throws SQLException {

        String idMovie;
        String place = "";
        String result = "";

        String sqlQuery = "Select id from mydatabase.movies where date= ? and title = ? and type = ?";

        PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
        preparedStmt.setString(1, date);
        preparedStmt.setString(2, title);
        preparedStmt.setString(3, type);
        ResultSet rs = preparedStmt.executeQuery();

        if (rs.next()) {
            idMovie = rs.getString("id");
        }
        else
            return "Error";

        sqlQuery = "Select place from mydatabase.reservation where id_movie = ?";
        preparedStmt = connection.prepareStatement(sqlQuery);

        preparedStmt.setString(1, idMovie);

        rs = preparedStmt.executeQuery();

        while (rs.next()) {
            place = rs.getString("place");
            result += place + "#";
        }

        return result;
    }

    private String checkUserInDB(String login, String pass) throws SQLException {

        String result = "";
        String role = "#";
        int idUser = 0;

        String sqlQuery = "Select id,role from mydatabase.users where email= ? and pass = ?";

        PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
        preparedStmt.setString(1, login);
        preparedStmt.setString(2, pass);
        ResultSet rs = preparedStmt.executeQuery();

        if (rs.next()) {
            idUser = rs.getInt("id");
            role = rs.getString("role");

            result = String.valueOf(idUser) + " " + role;

            return result;
        } else {
            result = String.valueOf(idUser) + " " + role;
            return result;
        }

    }

    private String getMovieId(String date, String title, String type) throws SQLException {

        String result = "";

        String sqlQuery = "Select id from mydatabase.movies where date = ? and title = ? and type = ?";

        PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
        preparedStmt.setString(1, date);
        preparedStmt.setString(2, title);
        preparedStmt.setString(3, type);
        ResultSet rs = preparedStmt.executeQuery();

        if (rs.next()) {
            result = rs.getString("id");
        }
        else
            result = "0";

        return result;
    }

    private String getMovies() throws SQLException {

        String result = "";

        String date;
        String title;
        String type;

        String sqlQuery = "Select * from mydatabase.movies";

        PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
        ResultSet rs = preparedStmt.executeQuery();

        while (rs.next()) {
            date = rs.getString("date");
            title = rs.getString("title");
            type = rs.getString("type");

            result += date + "@" + title + "@" + type + "#";
        }

        return result;
    }

    private String getUsers() throws SQLException {

        String result = "";

        String id;
        String email;
        String firstName;
        String lastName;
        String age;
        String phone;

        String sqlQuery = "Select * from mydatabase.users";

        PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
        ResultSet rs = preparedStmt.executeQuery();

        while (rs.next()) {
            id = rs.getString("id");
            email = rs.getString("email");
            firstName = rs.getString("first_name");
            lastName = rs.getString("last_name");
            age = rs.getString("age");
            phone = rs.getString("phone");

            result += id + "&" + email + "&" + firstName + "&" + lastName + "&" + age + "&" + phone + "#";
        }

        return result;
    }

    private String getMoviesWithId() throws SQLException {

        String result = "";

        String date;
        String title;
        String type;
        String id;

        String sqlQuery = "Select * from mydatabase.movies";

        PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
        ResultSet rs = preparedStmt.executeQuery();

        while (rs.next()) {
            id = rs.getString("id");
            date = rs.getString("date");
            title = rs.getString("title");
            type = rs.getString("type");

            result += id + "@" + date + "@" + title + "@" + type + "#";
        }

        return result;
    }

    private String getReservationForUser(String idUser) throws SQLException {

        String result = "";

        String idMovie;
        String place;
        String confirm;

        String sqlQuery = "Select * from mydatabase.reservation where id_user = ?";

        PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
        preparedStmt.setString(1, idUser);
        ResultSet rs = preparedStmt.executeQuery();

        while (rs.next()) {
            idMovie = rs.getString("id_movie");
            place = rs.getString("place");
            confirm = rs.getString("confirm");

            result += idMovie + "@" + place + "@" + confirm +"#";
        }

        return result;
    }

    private String getReservations() throws SQLException {

        String result = "";

        String idRes;
        String idMovie;
        String idUser;
        String place;
        String confirm;

        String sqlQuery = "Select * from mydatabase.reservation";

        PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
        ResultSet rs = preparedStmt.executeQuery();

        while (rs.next()) {
            idRes = rs.getString("id_reservation");
            idMovie = rs.getString("id_movie");
            idUser = rs.getString("id_user");
            place = rs.getString("place");
            confirm = rs.getString("confirm");

            result += idRes + "@" + idMovie + "@" + idUser + "@" + place + "@" + confirm +"#";
        }

        return result;
    }

    private String checkUserInDbByEmail(String reg_email) throws java.sql.SQLException {

        String emailDB = null;
        String sqlQuery = "Select email from mydatabase.users where email= ?";

        PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
        preparedStmt.setString(1, reg_email);
        ResultSet rs = preparedStmt.executeQuery();


        if (!rs.next()) {
            return "False";
        } else
            return "True";
    }

    private String addUserToDB(String email, String firstName, String lastName, String age, String phone, String pass) throws java.sql.SQLException {

        String sql = "INSERT INTO mydatabase.users (email, first_name, last_name, age, phone, pass, role)"
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement preparedStmt = connection.prepareStatement(sql);

        preparedStmt.setString(1, email);
        preparedStmt.setString(2, firstName);
        preparedStmt.setString(3, lastName);
        preparedStmt.setString(4, age);
        preparedStmt.setString(5, phone);
        preparedStmt.setString(6, pass);
        preparedStmt.setString(7, "Customer");

        preparedStmt.execute();

        return "Succses";
    }

    private String addMovieToDB(String date, String title, String type) throws java.sql.SQLException {

        String sql = "INSERT INTO mydatabase.movies (date, title, type)"
                + "VALUES (?, ?, ?)";

        PreparedStatement preparedStmt = connection.prepareStatement(sql);

        preparedStmt.setString(1, date);
        preparedStmt.setString(2, title);
        preparedStmt.setString(3, type);

        preparedStmt.execute();

        return "Succses";
    }


    private String deleteMovieById(String id) throws java.sql.SQLException {

        String sql = "DELETE FROM mydatabase.movies WHERE id= ?";

        PreparedStatement preparedStmt = connection.prepareStatement(sql);

        preparedStmt.setString(1, id);

        preparedStmt.execute();

        return "Succses";
    }
}


