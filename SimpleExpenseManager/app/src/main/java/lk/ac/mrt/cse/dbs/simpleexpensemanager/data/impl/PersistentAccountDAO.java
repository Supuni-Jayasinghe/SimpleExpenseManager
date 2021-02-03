package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.DBHelper;

public class PersistentAccountDAO implements AccountDAO {
    DBHelper dbHelper;

    public PersistentAccountDAO(Context context){
        dbHelper = new DBHelper(context);
    }

    @Override
    public List<String> getAccountNumbersList(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] column = {"accountNo"};
        Cursor cursor = db.query("Accounts", column, null, null, null, null, null);
        List accountNoList = new ArrayList<>();

        while(cursor.moveToNext()){
            String itemID = cursor.getString(cursor.getColumnIndexOrThrow("accountNo"));
            accountNoList.add(itemID);
        }

        cursor.close();
        return accountNoList;
    }

    @Override
    public List<Account> getAccountsList(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] column = {"accountNo", "bankName", "accountHolderName", "balance"};
        Cursor cursor = db.query("Accounts", column, null, null, null, null, null);
        List accountList = new ArrayList<>();

        while(cursor.moveToNext()){
            String accountNo = cursor.getString(cursor.getColumnIndexOrThrow("accountNo"));
            String bankName = cursor.getString(cursor.getColumnIndexOrThrow("bankName"));
            String accountHolderName = cursor.getString(cursor.getColumnIndexOrThrow("accountHolderName"));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));

            Account account = new Account(accountNo, bankName, accountHolderName, balance);

            accountList.add(account);
        }

        cursor.close();
        return accountList;
    }

    @Override
    public Account getAccount(String accNo) throws InvalidAccountException{
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] column = {"accountNo", "bankName", "accountHolderName", "balance"};
        String[] arg = {accNo};
        Cursor cursor = db.query("Accounts", column, "accountNo = ?", arg, null, null, null);

        Account account = null;

        while(cursor.moveToNext()){
            String accountNo = cursor.getString(cursor.getColumnIndexOrThrow("accountNo"));
            String bankName = cursor.getString(cursor.getColumnIndexOrThrow("bankName"));
            String accountHolderName = cursor.getString(cursor.getColumnIndexOrThrow("accountHolderName"));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));

            account = new Account(accountNo, bankName, accountHolderName, balance);
        }

        cursor.close();
        return account;
    }

    @Override
    public void addAccount(Account account) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("accountNo",account.getAccountNo());
        values.put("bankName",account.getBankName());
        values.put("accountHolderName",account.getAccountHolderName());
        values.put("balance",account.getBalance());

        long row = database.insert("Accounts", null, values);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String selection = "accountNo = ?";
        String[] args = { accountNo };
        database.delete("Accounts", selection, args);
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {

        if(accountNo ==null){
            throw new InvalidAccountException("Invalid Account Number");

        }
        Account account = dbHelper.getAccount(accountNo);
        double balance = account.getBalance();
        if(expenseType == ExpenseType.INCOME){
            account.setBalance(balance+amount);
        }else if (expenseType == ExpenseType.EXPENSE){
            account.setBalance(balance-amount);

        }
        if(account.getBalance()<0 ){
            throw new InvalidAccountException("Insufficient credit");
        }

        else{
            dbHelper.UpdateAccount(account);
        }

    }
}
