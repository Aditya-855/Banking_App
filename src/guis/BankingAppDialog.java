package guis;

import db_objs.MyJDBC;
import db_objs.Transaction;
import db_objs.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;

/*
    Display a custom dialog for our BankingAppGui
 */
public class BankingAppDialog extends JDialog implements ActionListener {
    private User user;
    private BankingAppGui bankingAppGui;
    private JLabel balanceLabel,enterAmountLable,enterUserLabel;
    private JTextField enterAmountField,enterUserField;
    private JButton actionButton;
    private JPanel pastTransactionsPanel;
    private ArrayList<Transaction> pastTransactions;

    public BankingAppDialog(BankingAppGui bankingAppGui,User user){
        //set the size
        setSize(400,400);

        //add focus to the dialog(can't interact with anything else until dialog is closed)
        setModal(true);

        //load in the center of our banking gui
        setLocationRelativeTo(bankingAppGui);

        //when user closes dialog ,it releases its  resources that are being used
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        //prevents dialog from being resized
        setResizable(false);

        //allows us to manually specify the size and position of each component
        setLayout(null);

        //we will need reference to our gui so that we can update the current balance
        this.bankingAppGui=bankingAppGui;

        //we will need access to the user info to make updates to our database or retrieve data about the user
        this.user=user;
    }

    public void addCurrentBalanceAndAmount(){
        //balance label
        balanceLabel=new JLabel("Balance: ₹" + user.getCurrentBalance());
        balanceLabel.setBounds(0,10,getWidth()-20,20);
        balanceLabel.setFont(new Font("Dialog",Font.BOLD,16));
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(balanceLabel);

        //enter amount label
        enterAmountLable=new JLabel("Enter Amount:");
        enterAmountLable.setBounds(0,50,getWidth()-20,20);
        enterAmountLable.setFont(new Font("Dialog",Font.BOLD,16));
        enterAmountLable.setHorizontalAlignment(SwingConstants.CENTER);
        add(enterAmountLable);

        //enter amount field
        enterAmountField=new JTextField();
        enterAmountField.setBounds(15,80,getWidth()-50,40);
        enterAmountField.setFont(new Font("Dialog",Font.BOLD,20));
        enterAmountField.setHorizontalAlignment(SwingConstants.CENTER);
        add(enterAmountField);
    }

    public void addActionButton(String actionButtonType){
        actionButton=new JButton(actionButtonType);
        actionButton.setBounds(15,300,getWidth()-50,40);
        actionButton.setFont(new Font("Dialog",Font.BOLD,20));
        actionButton.addActionListener(this);
        add(actionButton);
    }
    public void addUserField(){
        //enter user label
        enterUserLabel=new JLabel("Enter User");
        enterUserLabel.setBounds(0,160,getWidth()-20,20);
        enterUserLabel.setFont(new Font("Dialog",Font.BOLD,16));
        enterUserLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(enterUserLabel);

        //enter user field
        enterUserField=new JTextField();
        enterUserField.setBounds(15,190,getWidth()-50,40);
        enterUserField.setFont(new Font("Dialog",Font.BOLD,20));
        enterUserField.setHorizontalAlignment(SwingConstants.CENTER);
        add(enterUserField);
    }

    public void addPastTransactionComponents(){
        //container where we will store each transaction
        pastTransactionsPanel=new JPanel();

        //make layout 1x1
        pastTransactionsPanel.setLayout(new BoxLayout(pastTransactionsPanel,BoxLayout.Y_AXIS));

        //add scrollability to the container
        JScrollPane scrollPane=new JScrollPane(pastTransactionsPanel);

        //display the vertical scroll only when it is required
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.setBounds(0,20,getWidth()-15,getHeight()-40);

        scrollPane.setVisible(true);

        //perform database call to retrieve all of the past transaction and store into array list
        pastTransactions=MyJDBC.getPastTransaction(user);

        //iterate through the list and add to the gui
        for (int i=0;i<pastTransactions.size();i++){
            //store current transaction
            Transaction pastTransaction=pastTransactions.get(i);

            //create a container to store an  individual transaction
            JPanel pastTransactionContainer=new JPanel();
            pastTransactionContainer.setLayout(new BorderLayout());

            //create transaction type label
            JLabel transactionTypeLable=new JLabel(pastTransaction.getTransactionType());
            transactionTypeLable.setFont(new Font("Dialog",Font.BOLD,20));

            //create transaction amount label
            JLabel transactionAmountLabel=new JLabel(String.valueOf(pastTransaction.getTransactionAmount()));
            transactionAmountLabel.setFont(new Font("Dialog",Font.BOLD,20));

            //create transaction date label
            JLabel transactionDateLabel=new JLabel(String.valueOf(pastTransaction.getTransactionDate()));
            transactionDateLabel.setFont(new Font("Dialog",Font.BOLD,20));

//            JLabel transactionTimeLabel=new JLabel(String.valueOf(pastTransaction.getTransactionTime()));
//            transactionTimeLabel.setFont(new Font("Dialog",Font.BOLD,20));

            //add to the container
            pastTransactionContainer.add(transactionTypeLable,BorderLayout.WEST);//place this on the west side
            pastTransactionContainer.add(transactionAmountLabel,BorderLayout.EAST);
            pastTransactionContainer.add(transactionDateLabel,BorderLayout.SOUTH);
           // pastTransactionContainer.add(transactionTimeLabel,BorderLayout.NORTH);

            //give a white background to each container
            pastTransactionContainer.setBackground(Color.WHITE);

            //give a black border to each transaction container
            pastTransactionContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            //add transaction component to the transaction panel
            pastTransactionsPanel.add(pastTransactionContainer);
        }

        //add to the dialog
        add(scrollPane);
    }

    private void handleTransaction(String transactionType,float amountVal){
        Transaction transaction;

        if (transactionType.equalsIgnoreCase("Deposit")){
            //deposit transaction type
            //add to current balance
            user.setCurrentBalance(user.getCurrentBalance().add(new BigDecimal(amountVal)));

            //create transaction
            //we leave date null because we are going to be using the NOW() in sql which will get the current date
            transaction=new Transaction(user.getId(),transactionType,new BigDecimal(amountVal),null);
        }else {
            //withdraw transaction type
            //subtract from current balance
            user.setCurrentBalance(user.getCurrentBalance().subtract(new BigDecimal(amountVal)));

            //we want to show a negative sign for the amount val when withdrawing
            transaction=new Transaction(user.getId(),transactionType,new BigDecimal(-amountVal),null);
        }
        //update database
        if (MyJDBC.addTransactionToDatabase(transaction) && MyJDBC.updateCurrentBalance(user)){
            //show success dialog
            JOptionPane.showMessageDialog(this,transactionType + " Successfully!");

            //reset the fields
            resetFieldsAndUpdateCurrentBalance();
        }else {
            //show failure dialog
            JOptionPane.showMessageDialog(this,transactionType + " Failed....");
        }
    }

    private void resetFieldsAndUpdateCurrentBalance(){
        //reset fields
        enterAmountField.setText("");

        //only appears when the transfer is clicked
        if (enterUserField != null){
            enterUserField.setText("");
        }
        //update current balance on dialog
        balanceLabel.setText("Balance: ₹" + user.getCurrentBalance());

        //update current balance on main gui
        bankingAppGui.getCurrentBalanceField().setText("₹" + user.getCurrentBalance());
    }

    private void handleTransfer(User user,String transferedUser,float amount){
        //attempt to perform transfer
        if (MyJDBC.transfer(user, transferedUser, amount)){
            JOptionPane.showMessageDialog(this,"Transfer Success!");
            resetFieldsAndUpdateCurrentBalance();
        }else {
            //show failure dialog
            JOptionPane.showMessageDialog(this,"Transfer Failed.....");
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String buttonPressed=e.getActionCommand();

        //get Amount val
        float amountVal=Float.parseFloat(enterAmountField.getText());

        //pressed deposit
        if (buttonPressed.equalsIgnoreCase("Deposit")){
            //we want to handle the deposit transaction
            handleTransaction(buttonPressed,amountVal);
        }else {
            //pressed withdraw or transfer


            //validate input by making sure that withdraw or transfer amount is less than current balance
            //if result is -1 it means that the entered amount is more, 0 means they are equal, and 1 means that
            //the entered amount is less
            int result=user.getCurrentBalance().compareTo(BigDecimal.valueOf(amountVal));
            if (result < 0){
                //display error dialog
                JOptionPane.showMessageDialog(this,"Error: Input value is  more than current balance");
                return;
            }
            //check to see if withdraw or transfer was pressed
            if (buttonPressed.equalsIgnoreCase("Withdraw")){
                handleTransaction(buttonPressed,amountVal);
            }else {
                //transfer
                String transferdeUser=enterUserField.getText();

                //handle transfer
                handleTransfer(user,transferdeUser,amountVal);
            }
        }
    }
}
