package com.example.expensetracker.util;

import com.example.expensetracker.data.model.Transaction;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParser {

    private static final List<String> DEBIT_KEYWORDS = Arrays.asList(
            "spent", "debited", "debit", "payment", "payment of", "transferred",
            "used", "withdrawn", "charged", "deducted", "txn", "bill payment", "paid"
    );

    private static final List<String> TRANSACTION_TYPES = Arrays.asList(
            "IMPS", "NEFT", "RTGS", "UPI", "XFER", "ECS", "ACH", "BillPay"
    );

    private static final Map<String, List<String>> CATEGORY_MAP = new HashMap<String, List<String>>() {{
        put("Food & Dining", Arrays.asList("Swiggy", "Zomato", "Dominos", "pizzahut"));
        put("Shopping & Retail", Arrays.asList("Amazon", "Flipkart", "Myntra"));
        put("Travel / Transport", Arrays.asList("Uber", "Ola", "IRCTC"));
        put("Utilities / Bills", Arrays.asList("Electricity", "Gas", "Water"));
        put("EMI / Loans", Arrays.asList("EMI", "ECS"));
        put("Cash Withdrawals", Arrays.asList("ATM", "Withdrawn"));
        put("Fees & Charges", Arrays.asList("maintenance charge", "annual fee"));
    }};

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?:INR|Rs\\.?|₹)\\s?([\\d,]+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE);

    public static Transaction parseTransactionFromSms(String smsBody) {
        String lowerCaseSms = smsBody.toLowerCase();

        boolean isDebit = DEBIT_KEYWORDS.stream().anyMatch(lowerCaseSms::contains);
        if (!isDebit) {
            return null;
        }

        Matcher amountMatcher = AMOUNT_PATTERN.matcher(smsBody);
        if (!amountMatcher.find()) {
            return null;
        }

        double amount = Double.parseDouble(amountMatcher.group(1).replace(",", ""));
        String type = "Unknown";
        String merchant = "Unknown";
        String category = "Others";

        for (String txnType : TRANSACTION_TYPES) {
            if (lowerCaseSms.contains(txnType.toLowerCase())) {
                type = txnType;
                break;
            }
        }

        for (Map.Entry<String, List<String>> entry : CATEGORY_MAP.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerCaseSms.contains(keyword.toLowerCase())) {
                    category = entry.getKey();
                    merchant = keyword;
                    break;
                }
            }
            if (!"Others".equals(category)) {
                break;
            }
        }

        return new Transaction(smsBody, amount, type, merchant, category, 0); // Date will be set by caller
    }
}
