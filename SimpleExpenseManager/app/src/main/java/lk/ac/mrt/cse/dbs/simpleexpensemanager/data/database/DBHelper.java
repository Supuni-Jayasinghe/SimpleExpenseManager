package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class DBHelper extends SQLiteOpenHelper{

    public  static final String dbName = "180276A";
    public  static final String account = "accountTable";
    public  static final String transaction = "transactionTable";

    public DBHelper(Context context){
        super(context,dbName,null,1);
        SQLiteDatabase db = this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+account+" (accountNo TEXT(30) PRIMARY KEY,bankName TEXT(30),accountHolderName TEXT(30),balance REAL) ");
        db.execSQL("CREATE TABLE "+transaction+" (accountNo TEXT(30) ,date date, expenseType TEXT(20),amount REAL,FOREIGN KEY (accountNo) REFERENCES "+account+"(accountNo))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+account);
        db.execSQL("DROP TABLE IF EXISTS "+transaction);
        onCreate(db);
    }

    public boolean AddAccount(Account acc){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo",acc.getAccountNo());
        contentValues.put("bankName",acc.getBankName());
        contentValues.put("accountHolderName",acc.getAccountHolderName());
        contentValues.put("balance",acc.getBalance());
        long result = db.insert(account,null,contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }


    public boolean UpdateAccount(Account acc){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo",acc.getAccountNo());
        contentValues.put("bankName",acc.getBankName());
        contentValues.put("accountHolderName",acc.getAccountHolderName());
        contentValues.put("balance",acc.getBalance());
        long result = db.update(account,contentValues,"accountNo = ?",new String[]{acc.getAccountNo()});
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Account getAccount(String accNumber){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+account+" WHERE accountNo = ?",new String[]{accNumber});
        Account account = null;
        if(res.getCount() == 0){
            return account;
        }else{
            while(res.moveToNext()){
                String accountNo = res.getString(0);
                String bankName = res.getString(1);
                String accountHolderName = res.getString(2);
                double balance = res.getDouble(3);
                account = new Account(accountNo,bankName,accountHolderName,balance);
            }
            return account;
        }
    }

    public ArrayList<Account> getAllAccounts(){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+account,null);
        ArrayList<Account> accountList=new ArrayList<>();
        if(cursor.getCount()==0){
            return accountList;
        }else{

            while(cursor.moveToNext()){
                String accountNo = cursor.getString(0);
                String bankName = cursor.getString(1);
                String accountHolderName = cursor.getString(2);
                double balance = cursor.getDouble(3);
                accountList.add(new Account(accountNo,bankName,accountHolderName,balance));
            }
            return accountList;
        }
    }

    public boolean deleteAccount(String accountNo){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(account,"accountNo = "+accountNo,null) > 0;

    }

    public boolean logTransaction(Transaction t){

        DateFormat format = new SimpleDateFormat("m-d-yyyy", Locale.ENGLISH);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo",t.getAccountNo());
        contentValues.put("date",format.format(t.getDate()));
        contentValues.put("expenseType",t.getExpenseType().toString());
        contentValues.put("amount",t.getAmount());


        long res = db.insert(transaction,null,contentValues);
        if(res == -1){
            return false;
        }else{
            return true;
        }



    }

    public ArrayList<Transaction> getTransactions(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+transaction,null);
        return populateTransactions(res);
    }

    public ArrayList<Transaction> getTransactions(int limit){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+transaction+" LIMIT "+limit,null);
        return populateTransactions(res);
    }



    private ArrayList<Transaction> populateTransactions(Cursor res){

        ArrayList<Transaction> transactionList=new ArrayList<>();
        DateFormat format = new SimpleDateFormat("m-d-yyyy", Locale.ENGLISH);
        if(res.getCount()==0){
            return transactionList;
        }else{

            while(res.moveToNext()){
                String accountNo = res.getString(0);
                Date date = new Date();
                ExpenseType expenseType = ExpenseType.valueOf(res.getString(2));
                try {
                    date =  format.parse(res.getString(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
//

                double amount = res.getDouble(3);
                transactionList.add(new Transaction(date,accountNo,expenseType,amount));
            }
            return transactionList;
        }
    }
}
