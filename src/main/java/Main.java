import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {
        JDAConnection jda = new JDAConnection();
    }

    public void testDatabase() {
        PostgreSQLJDBC database = new PostgreSQLJDBC();
        database.createTables();

        database.addMember("Lance");
        database.addMember("Steven");

        database.addPrediction(1, "GSW", "2021-05-21", 1);
        database.addPrediction(1, "GSW", "2021-05-21", 2);

        ArrayList<Integer> test1 = database.getPredictions("2021-05-21", 1);
        ArrayList<Integer> test2 = database.getPredictions("2021-05-21", -1);

        for(int i = 0; i < test1.size(); i++) {
            System.out.println(test1.get(i));
        }
        for(int j = 0; j < test2.size(); j++) {
            System.out.println(test2.get(j));
        }

        System.out.println(database.getPoints("Lance"));
        database.updatePoints("Lance", 2);
        System.out.println(database.getPoints("Lance"));
    }

}
