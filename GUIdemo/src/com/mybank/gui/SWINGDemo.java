package com.mybank.gui;

import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Locale;
import java.util.Scanner;

import javax.swing.*;

public class SWINGDemo {

    private final JEditorPane log;
    private final JButton show;
    private final JButton report;
    private final JComboBox<String> clients;

    public SWINGDemo() {

        log = new JEditorPane("text/html", "");
        log.setEditable(false);
        log.setPreferredSize(new Dimension(400, 250));

        show = new JButton("Show");
        report = new JButton("Report");

        clients = new JComboBox<>();

        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            Customer c = Bank.getCustomer(i);
            clients.addItem(c.getLastName() + ", " + c.getFirstName());
        }
    }

    private void launchFrame() {

        JFrame frame = new JFrame("MyBank clients");
        frame.setLayout(new BorderLayout());

        JPanel cpane = new JPanel(new GridLayout(1, 3));

        cpane.add(clients);
        cpane.add(show);
        cpane.add(report);

        frame.add(cpane, BorderLayout.NORTH);
        frame.add(log, BorderLayout.CENTER);

        show.addActionListener((ActionEvent e) -> {

            Customer current = Bank.getCustomer(clients.getSelectedIndex());

            StringBuilder html = new StringBuilder();

            html.append("<html>");
            html.append("<h2>")
                    .append(current.getLastName())
                    .append(", ")
                    .append(current.getFirstName())
                    .append("</h2>");
            html.append("<hr>");

            for (int i = 0; i < current.getNumberOfAccounts(); i++) {

                html.append("<b>Account ")
                        .append(i + 1)
                        .append("</b><br>");

                if (current.getAccount(i) instanceof SavingsAccount) {
                    html.append("Type: Savings<br>");
                } else if (current.getAccount(i) instanceof CheckingAccount) {
                    html.append("Type: Checking<br>");
                }

                html.append("Balance: $")
                        .append(current.getAccount(i).getBalance())
                        .append("<br><br>");
            }

            html.append("</html>");
            log.setText(html.toString());
        });

        report.addActionListener((ActionEvent e) -> {
            log.setText(buildReport());
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private String buildReport() {

        StringBuilder html = new StringBuilder();

        html.append("<html>");
        html.append("<h1>Bank Customer Report</h1>");
        html.append("<hr>");

        double totalBankBalance = 0;

        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {

            Customer c = Bank.getCustomer(i);

            html.append("<h3>")
                    .append(c.getLastName())
                    .append(", ")
                    .append(c.getFirstName())
                    .append("</h3>");

            double customerTotal = 0;

            for (int j = 0; j < c.getNumberOfAccounts(); j++) {

                double balance = c.getAccount(j).getBalance();
                customerTotal += balance;
                totalBankBalance += balance;

                html.append("- Account ")
                        .append(j + 1)
                        .append(": ")
                        .append(balance)
                        .append("<br>");
            }

            html.append("<b>Total for customer: $")
                    .append(customerTotal)
                    .append("</b><br><br>");
        }

        html.append("<hr>");
        html.append("<h2>Total Bank Balance: $")
                .append(totalBankBalance)
                .append("</h2>");

        html.append("</html>");

        return html.toString();
    }

    private static double readDoubleSafe(Scanner sc, String errorMsg) {
        try {
            return Double.parseDouble(sc.next());
        } catch (Exception e) {
            throw new IllegalStateException(errorMsg + " | Found: " + sc.next(), e);
        }
    }

    private static void loadData(String filename) {

        try (Scanner sc = new Scanner(new File(filename))) {

            sc.useLocale(Locale.US);

            int customers = sc.nextInt();

            for (int i = 0; i < customers; i++) {

                String firstName = sc.next();
                String lastName = sc.next();
                int accounts = sc.nextInt();

                Bank.addCustomer(firstName, lastName);
                Customer customer = Bank.getCustomer(i);

                for (int j = 0; j < accounts; j++) {

                    String type = sc.next().trim().toUpperCase();

                    switch (type) {

                        case "S": {
                            double balance = readDoubleSafe(sc, "Invalid savings balance");
                            double rate = readDoubleSafe(sc, "Invalid savings rate");

                            customer.addAccount(new SavingsAccount(balance, rate));
                            break;
                        }

                        case "C": {
                            double balance = readDoubleSafe(sc, "Invalid checking balance");
                            double overdraft = readDoubleSafe(sc, "Invalid overdraft limit");

                            customer.addAccount(new CheckingAccount(balance, overdraft));
                            break;
                        }

                        default:
                            throw new IllegalStateException("Unknown account type: " + type);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {

        loadData("test.dat");

        SWINGDemo demo = new SWINGDemo();
        demo.launchFrame();
    }
}