package com.alliance.diceanalytics.constant;

public class ReportDetails {
    public enum ReportDuration {
        daily,
        weekly,
        monthly
    }

    public static String getReportName (ReportDuration reportDuration, Integer type) {

        switch (reportDuration){
            case daily:
                if (type==1)
                    return "ekycSPReferralFulfilment";
                else if (type ==4)
                    return  "HenrytoPrivilegeFulfilment";
                else if (type ==5)
                    return  "HenrytoPersonalFulfilment";
                else if (type == 7)
                    return "ekycSPMasterPromoCodeListing";
                return "";


            case weekly:
                if (type == 1)
                    return "ekycSPReferralPerformance";
                else if (type ==2)
                    return "CrossSellSoloCCtoPLoan";
                else if (type ==3)
                    return "CrossSellPLoantoSP";
                else if (type ==4)
                    return  "HenrytoPrivilegePerformance";
                else if (type ==5)
                    return  "HenrytoPersonalPerformance";
                else if (type==6)
                    return "PersonalInfoUpdate";
                else
                    return "";

            case monthly:
                if (type==0)
                    return "WhatsappChatbotReferral";
                else if (type==1)
                    return "ekycSPReferralFulfilment";
                else if (type ==2)
                    return "CrossSellPLoantoSPConversion";
                else if (type ==3)
                    return "CrossSellSoloCCtoPLoanConversion";
                else
                    return "";

            default:
                return "";
        }

    }



}
