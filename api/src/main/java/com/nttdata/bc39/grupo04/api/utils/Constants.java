package com.nttdata.bc39.grupo04.api.utils;

public final class Constants {
    public static final String CODE_ACCOUNT_PERSONAL = "PERSONAL";
    public static final int LENGHT_CODE_PERSONAL_CUSTOMER = 8;

    public static final String CODE_ACCOUNT_EMPRESARIAL = "EMPRESARIAL";
    public static final int LENGHT_CODE_EMPRESARIAL_CUSTOMER = 11;

    public static final String CODE_PRODUCT_CUENTA_AHORRO = "01";
    public static final String CODE_PRODUCT_CUENTA_CORRIENTE = "02";
    public static final String CODE_PRODUCT_PLAZO_FIJO = "03";
    public static final String CODE_PRODUCT_TARJETA_CREDITO = "04";
    public static final String CODE_PRODUCT_CREDITO_PERSONAL = "05";
    public static final String CODE_PRODUCT_CREDITO_EMPRESARIAL = "06";
    public static final String CODE_PRODUCT_PERSONAL_VIP_AHORRO = "07";
    public static final String CODE_PRODUCT_EMPRESA_PYME_CORRIENTE = "08";

    public static final int MIN_DEPOSIT_AMOUNT = 10;
    public static final int MAX_DEPOSIT_AMOUNT = 5000;
    public static final int MIN_WITHDRAWAL_AMOUNT = 10;
    public static final int MAX_WITHDRAWAL_AMOUNT = 5000;

    public static final int MIN_TRANSFERENCE_AMOUNT = 5;
    public static final int MAX_TRANSFERENCE_AMOUNT = 6000;


    public static final int MAX_TRANSACCION_FREE = 3;
    public static final double COMISSION_AMOUNT_PER_TRANSACTION = 10.0;
    public static final String ACCOUNT_NUMBER_OF_ATM = "CAJEROX3SMP";
    public static final int INITIAL_AMOUNT_OF_ATM = 20000;

    public static final int MIN_PAYMENT_CREDIT_AMOUNT = 1;
    public static final int MIN_PAYMENT_CREDIT_CARD_AMOUNT = 1;
    public static final int MIN_CHARGE_CREDIT_CARD_AMOUNT = 1;

    public static final double MIN_AMOUNT_DAILY_AVERAGE = 0;
    public static final double MIN_AMOUNT_MANTENANCE_FEET = 0;


    public static String getNameProduct(String productId) {
        switch (productId) {
            case CODE_PRODUCT_CUENTA_AHORRO:
                return "CUENTA DE AHORRO";
            case CODE_PRODUCT_CUENTA_CORRIENTE:
                return "CUENTA CORRIENTE";
            case CODE_PRODUCT_PLAZO_FIJO:
                return "PLAZA FIJO";
            case CODE_PRODUCT_TARJETA_CREDITO:
                return "TARJETA DE CREDITO";
            case CODE_PRODUCT_CREDITO_PERSONAL:
                return "CREDITO PERSONAL";
            case CODE_PRODUCT_CREDITO_EMPRESARIAL:
                return "CREDITO EMPRESARIAL";
            case CODE_PRODUCT_PERSONAL_VIP_AHORRO:
                return "PERSONAL VIP AHORRO";
            case CODE_PRODUCT_EMPRESA_PYME_CORRIENTE:
                return "EMPRESA PYME CORRIENTE";
            default:
                return "SIN NOMBRE";
        }
    }
}
