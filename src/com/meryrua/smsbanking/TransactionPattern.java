package com.meryrua.smsbanking;

public class TransactionPattern {

    private String pattern;
    public static final int GROUP_NUMBER = 8;
    public static final int GROUP_CARD_NUMBER = 0;
    public static final int GROUP_DATE_NUMBER = 1;
    public static final int GROUP_PLACE_NUMBER = 2;
    public static final int GROUP_TRANSACTION_VALUE_NUMBER = 3;
    public static final int GROUP_TRANSACTION_CURRENCY_NUMBER = 4;
    public static final int GROUP_FUND_VALUE_NUMBER = 5;
    public static final int GROUP_FUND_CURRENCY_NUMBER = 6;
    public static final int GROUP_BANK_NUMBER = 7;
    
    private int[] groupPattern;
    
    public TransactionPattern() {
        pattern = null;
        groupPattern = new int[GROUP_NUMBER];
    }
    
    public TransactionPattern(String string, int groups[]) {
        if (string != null) {
            pattern = new String (string);
        }
        groupPattern = groups;
    }
    
    public void  setPattern(String string) {
        if (string != null) {
            pattern = new String (string);
        }
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public void setValue(int number, int value) {
        groupPattern[number] = value;
    }
    
    public int getValue(int number) {
        return groupPattern[number];
    }
    
    
    
}
