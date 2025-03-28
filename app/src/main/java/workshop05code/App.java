package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            System.err.println("Logging configuration could not be loaded.");
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());
    
    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            System.out.println("Wordle created and connected.");
        } else {
            System.out.println("Not able to connect. Sorry!");
            logger.severe("Database connection failed.");
            return;
        }
        if (wordleDatabaseConnection.createWordleTables()) {
            System.out.println("Wordle structures in place.");
        } else {
            System.out.println("Not able to launch. Sorry!");
            logger.severe("Failed to create Wordle structures.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (line.matches("^[a-z]{4}$")) {
                    wordleDatabaseConnection.addValidWord(i, line);
                    logger.info("Valid word added: " + line);
                } else {
                    logger.severe("Invalid word detected in data.txt: " + line);
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println("Not able to load. Sorry!");
            logger.log(Level.SEVERE, "Error loading words from file.", e);
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4 letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {
                System.out.println("You've guessed '" + guess + "'.");

                if (guess.matches("^[a-z]{4}$")) {
                    if (wordleDatabaseConnection.isValidWord(guess)) {
                        System.out.println("Success! It is in the list.\n");
                    } else {
                        System.out.println("Sorry. This word is NOT in the list.\n");
                        logger.warning("Invalid guess: " + guess);
                    }
                } else {
                    System.out.println("Invalid input. Please enter a 4-letter word.\n");
                    logger.warning("User entered an invalid word: " + guess);
                }

                System.out.print("Enter a 4 letter word for a guess or q to quit: ");
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            System.out.println("An error occurred. Please try again later.");
            logger.log(Level.WARNING, "Scanner input error.", e);
        }
    }
}
