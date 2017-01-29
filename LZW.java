import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.print.DocFlavor;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by menna on 10/24/2016.
 */
public class LZW extends Application {

        public static ArrayList<String> constructDict() {
            ArrayList<String> dictionary = new ArrayList<>();
            int capital = 65, lowercase = 97;
            for (int i = 0; i < 26; i++) {
                String symbol = Character.toString((char)capital);
                dictionary.add(i, symbol);
                capital++;
            }
            for (int i = 26; i < 52; i++) {
                String symbol = Character.toString((char)lowercase);
                dictionary.add(i, symbol);
                lowercase++;
            }
            return dictionary;
        }

        public static File Compress(String path) { // change to file path
            String content = ""; String compressedSt = "";
            try {
                content = new Scanner(new File(path)).useDelimiter("\\Z").next(); // convert everything in file into one string -> content
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            int stringLen = content.length();

            ArrayList<String> dictionary = constructDict();
            ArrayList<Integer> compressedTags = new ArrayList<Integer>();

            String t = Character.toString(content.charAt(0));
            int n = dictionary.indexOf(t);

            for (int i = 1; i < stringLen; i++) {
                String s = Character.toString(content.charAt(i));
                if (dictionary.indexOf(t + s) != -1) { // if t + s found
                    t = t + s;
                    n = dictionary.indexOf(t);
                } else {
                    compressedTags.add(n);
                    compressedSt += (Integer.toString(n));
                    compressedSt += ("\n");
                    dictionary.add(t + s);
                    t = s;
                    n = dictionary.indexOf(t);
                }

            }
            if (n != -1) {
                compressedTags.add(n);
                compressedSt += (Integer.toString(n));
                compressedSt += "\n";
            }


            System.out.println(compressedSt);

            File original = new File(path);
            String dir = original.getParent();
            File compressed = new File(dir + "\\compressed.txt");

            try(PrintWriter out = new PrintWriter(dir + "\\compressed.txt")) {
                out.println(compressedSt); // write tags
                out.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return compressed;
            //return compressedTags;
        }


        public static File deCompress(String path) { //change to file

            ArrayList<Integer> tags = new ArrayList<>();
            try(Scanner cin = new Scanner(new File(path))) {
                while (cin.hasNext()) {
                    String num = cin.nextLine(); Integer tag = Integer.parseInt(num);
                    tags.add(tag);
                }
            }
            catch(FileNotFoundException f) {
                f.printStackTrace();
            }

            String original = "";
            ArrayList<String> dictionary = constructDict();

            original += dictionary.get(tags.get(0));

            String newEntry = "";
            for (int i = 1; i < tags.size(); i++) {
                if (tags.get(i) >= dictionary.size()) {
                    newEntry = dictionary.get(tags.get(i - 1)) + dictionary.get(tags.get(i - 1)).charAt(0);
                    dictionary.add(tags.get(i), newEntry);
                    original += newEntry;
                }
                else {
                    original += dictionary.get(tags.get(i));
                    //System.out.println(original);
                    newEntry = dictionary.get(tags.get(i - 1)) + dictionary.get(tags.get(i)).charAt(0);
                    dictionary.add(newEntry);
                }
            }


            System.out.println(original);

            File de = new File(path);
            String dir = de.getParent();
            File decompressed = new File(dir + "\\decompressed.txt");

            try(PrintWriter out = new PrintWriter(dir + "\\decompressed.txt")) {
                out.println(original);
                out.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            //  File decompressed = new File("decompressed.txt");
            return decompressed;
        }



        Button browse, compress, decompress;

        public static void main(String args[]) {
            launch(args);
        }
        private Desktop desktop = Desktop.getDesktop();

        private void openFile(File file) {
            try {
                desktop.open(file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            // main
            primaryStage.setTitle("LZW Compressor/Decompressor Tool");
            javafx.scene.control.Label home = new javafx.scene.control.Label("Choose file to compress/decompress:");
            javafx.scene.control.TextField fillWithPath = new javafx.scene.control.TextField();
            fillWithPath.setMaxWidth(200);
            browse = new Button("Browse");
            FileChooser choose = new FileChooser();
            browse.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    FileChooser.ExtensionFilter extFilter =
                            new FileChooser.ExtensionFilter("TEXT files (*.txt)", "*.txt"); // only choose text files
                    choose.getExtensionFilters().add(extFilter);

                    // show open file dialog
                    File file = choose.showOpenDialog(primaryStage);
                    fillWithPath.setText(file.getAbsolutePath());

                }
            });
            VBox layout1  = new VBox(20);
            layout1.setAlignment(Pos.CENTER);
            // StackPane layout1 = new StackPane();
            compress = new Button("Compress");
            decompress = new Button("Decompress");

            compress.setOnAction(e -> {
                if (fillWithPath.getCharacters().toString().isEmpty()) { // FILE NOT CHOSEN
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Please choose a file.");
                    alert.show();
                }
                else if(!(new File(fillWithPath.getCharacters().toString()).isFile())) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid path");
                    alert.show();

                }
                else {
                    File o = Compress(fillWithPath.getText());
                    openFile(o);
                }


            });
            decompress.setOnAction(e -> {
                if(fillWithPath.getCharacters().toString().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Please choose a file.");
                    alert.show();
                }
                else if(!(new File(fillWithPath.getCharacters().toString()).isFile())) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid path");
                    alert.show();
                }
                else {
                    File d = deCompress(fillWithPath.getText());
                    openFile(d);
                }
            });


            layout1.getChildren().addAll(home, fillWithPath, browse, compress, decompress);
            Scene scene = new Scene(layout1, 300, 250);
            scene.setFill(javafx.scene.paint.Color.BLACK); // THIS LINE IS REDUNDANT AND USELESS
            layout1.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.PEACHPUFF, CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY))); //THIS IS SO FUN OMG
            compress.setFont(javafx.scene.text.Font.font("Lucida Grande"));
            decompress.setFont(javafx.scene.text.Font.font("Lucida Grande"));
            browse.setFont(javafx.scene.text.Font.font("Lucida Grande"));
            fillWithPath.setFont(javafx.scene.text.Font.font("Lucida Grande"));
            home.setFont(javafx.scene.text.Font.font("Lucida Grande")); // gainsboro
            primaryStage.setScene(scene);
            primaryStage.show();


        }



    }
