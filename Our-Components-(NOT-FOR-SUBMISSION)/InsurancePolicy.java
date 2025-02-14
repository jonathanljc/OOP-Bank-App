import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.text.DecimalFormat;

/**
 * Abstract class representing an insurance policy.
 */
public abstract class InsurancePolicy {

    private String policyNumber;
    private LocalDate policyStartDate;
    private LocalDate policyEndDate;
    private CoverageOption coverageOption;
    private PolicyTenure policyTenure;
    private PremiumFrequency premiumFrequency;
    private int age;

    /**
     * Constructor for InsurancePolicy class.
     * @param startDateString The start date of the policy.
     * @param coverageOption The coverage option of the policy.
     * @param policyTenure The tenure of the policy.
     * @param premiumFrequency The premium payment frequency.
     * @param age The age of the policyholder.
     * @throws PolicyException if there is an error in policy creation.
     */
    public InsurancePolicy(String startDateString, CoverageOption coverageOption, PolicyTenure policyTenure,
                           PremiumFrequency premiumFrequency, int age) throws PolicyException {
        this.coverageOption = coverageOption;
        this.policyTenure = policyTenure;
        this.premiumFrequency = premiumFrequency;
        this.age = age;
        this.policyNumber = generatePolicyNumber();
        parseDates(startDateString);
    }

    // Getters for various policy attributes

    protected String getPolicyNumber() {
        return policyNumber;
    }

    protected LocalDate getPolicyStartDate() {
        return policyStartDate;
    }

    protected LocalDate getPolicyEndDate() {
        return policyEndDate;
    }

    protected CoverageOption getCoverageOption() {
        return coverageOption;
    }

    protected PolicyTenure getPolicyTenure() {
        return policyTenure;
    }

    protected PremiumFrequency getPremiumFrequency() {
        return premiumFrequency;
    }

    protected int getAge() {
        return age;
    }

    /**
     * Abstract method to calculate the base premium of the policy.
     * @return The base premium amount.
     */
    protected abstract double calculateBasePremium();

    // Private methods

    private String generatePolicyNumber() {
        return UUID.randomUUID().toString();
    }

    private void parseDates(String startDateString) throws PolicyException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate startDate = LocalDate.parse(startDateString, formatter);
            this.policyStartDate = startDate;
            this.policyEndDate = startDate.plusYears(this.policyTenure.getYears());
        } catch (DateTimeParseException e) {
            throw new PolicyException("Invalid date format. Please use 'yyyy-MM-dd'.");
        }
    }

    // Other methods

    /**
     * Checks if the policy is currently active.
     * @return True if active, false otherwise.
     */
    public boolean isPolicyActive() {
        LocalDate currentDate = LocalDate.now();
        return currentDate.isAfter(policyStartDate) && currentDate.isBefore(policyEndDate);
    }

    /**
     * Calculates various premiums associated with the policy.
     * @return A map containing premium details.
     * @throws PolicyException if there is an error in premium calculation.
     */
    public Map<String, Double> calculatePremium() throws PolicyException {
        Map<String, Double> premiums = new HashMap<>();
        try {
            // Calculate base premium and modifiers
            double basePremiumBeforeModifiers = calculateBasePremium();
            double basePremiumAfterModifiers = basePremiumBeforeModifiers;

            // Calculate premium per period
            double premiumPerPeriod = basePremiumAfterModifiers / getPremiumFrequency().getMonths();
            int totalPeriods = getPremiumFrequency().getMonths() * getPolicyTenure().getYears();

            // Calculate total premium and GST
            double totalPremium = premiumPerPeriod * totalPeriods;
            double gst = totalPremium * 0.09; // GST rate is 9%
            double totalPremiumWithGST = totalPremium + gst;

            // Calculate GST per period
            double gstPerPeriod = gst / totalPeriods;
            double premiumPerPeriodWithGST = premiumPerPeriod + gstPerPeriod;

            // Put the calculated values into the premiums map
            premiums.put("basePremiumBeforeModifiers", basePremiumBeforeModifiers);
            premiums.put("basePremiumAfterModifiers", basePremiumAfterModifiers);
            premiums.put("premiumPerPeriod", premiumPerPeriod);
            premiums.put("totalPremium", totalPremium);
            premiums.put("gst", gst);
            premiums.put("totalPremiumWithGST", totalPremiumWithGST);
            premiums.put("gstPerPeriod", gstPerPeriod);
            premiums.put("premiumPerPeriodWithGST", premiumPerPeriodWithGST);
        } catch (Exception e) {
            // If an exception occurs during the premium calculation, throw a PolicyException
            throw new PolicyException("Error calculating premiums: " + e.getMessage());
        }
        return premiums;
    }

    /**
     * Displays details of the insurance policy.
     * @return A string containing policy details.
     */
    public String displayPolicyDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPolicyType()).append(" Policy Details:\n");
        sb.append("Policy Number: ").append(getPolicyNumber()).append("\n");
        sb.append("Coverage Option: ").append(getCoverageOption()).append("\n");
        sb.append("Policy Tenure: ").append(getPolicyTenure()).append("\n");
        sb.append("Premium Frequency: ").append(getPremiumFrequency()).append("\n");
        sb.append("Policy Start Date: ").append(getPolicyStartDate()).append("\n");
        sb.append("Policy End Date: ").append(getPolicyEndDate()).append("\n");

        try {
            Map<String, Double> premiums = calculatePremium();
            double basePremiumAfterModifiers = premiums.get("basePremiumAfterModifiers");
            double premiumPerPeriod = premiums.get("premiumPerPeriod");
            double totalPremium = premiums.get("totalPremium");
            double gst = premiums.get("gst");
            double totalPremiumWithGST = premiums.get("totalPremiumWithGST");
            double gstPerPeriod = premiums.get("gstPerPeriod");
            double premiumPerPeriodWithGST = premiums.get("premiumPerPeriodWithGST");

            DecimalFormat df = new DecimalFormat("#.00");

            sb.append("Base Premium (Before Modifier): $").append(df.format(getCoverageOption().getValue())).append("\n");

            // Display age price added based on policy type
            if (this instanceof LifeInsurance) {
                sb.append("Age Price Added: $").append(df.format(((LifeInsurance) this).agePriceAdded())).append("\n");
                
            } else if (this instanceof HealthInsurance) {
                sb.append("Age Price Added: $").append(df.format(((HealthInsurance) this).agePriceAdded())).append("\n");
            } else if (this instanceof AccidentInsurance) {
                sb.append("Age Price Added: $").append(df.format(((AccidentInsurance) this).agePriceAdded())).append("\n");
            }

            // Display smoking price and injuries price based on policy type
            if (this instanceof LifeInsurance) {
                sb.append("Smoker Price: $").append(df.format(((LifeInsurance) this).isSmoker() ? 500.00 : 0.00)).append("\n");
            } else if (this instanceof HealthInsurance) {
                sb.append("Smoker Price: $").append(df.format(((HealthInsurance) this).isSmoker() ? 500.00 : 0.00)).append("\n");
            } else if (this instanceof AccidentInsurance) {
                sb.append("Injuries Price: $").append(df.format(((AccidentInsurance) this).hasPastInjuries() ? 1000.00 : 0.00)).append("\n");
            }
            sb.append("Base Premium (After Modifiers): $").append(df.format(basePremiumAfterModifiers)).append("\n");
            sb.append("Premium Per Period: $").append(df.format(premiumPerPeriod)).append("\n");
            sb.append("GST Per Period: $").append(df.format(gstPerPeriod)).append("\n"); // Display GST per period
            sb.append("Premium Per Period (With GST): $").append(df.format(premiumPerPeriodWithGST)).append("\n");
            sb.append("Total Premium: $").append(df.format(totalPremium)).append("\n");
            sb.append("GST (9%): $").append(df.format(gst)).append("\n");
            sb.append("Total Premium (With GST): $").append(df.format(totalPremiumWithGST)).append("\n");

        } catch (PolicyException e) {
            sb.append("Error calculating premiums: ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Abstract method to get the type of insurance policy.
     * @return The type of insurance policy.
     */
    protected abstract String getPolicyType();

    // Enumerations

    /**
     * Enum representing coverage options for insurance policies.
     */
    public enum CoverageOption {
        BASIC(1000),
        STANDARD(2000),
        PREMIUM(3000);

        private final int value;

        CoverageOption(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Enum representing policy tenures.
     */
    public enum PolicyTenure {
        FIVE_YEARS(5),
        TEN_YEARS(10),
        FIFTEEN_YEARS(15),
        TWENTY_YEARS(20);

        private final int years;

        PolicyTenure(int years){
            this.years = years;
        }

        public int getYears() {
            return years;
        }
    }

    /**
     * Enum representing premium payment frequencies.
     */
    public enum PremiumFrequency {
        MONTHLY(1),
        QUARTERLY(3),
        SEMI_ANNUALLY(6),
        ANNUALLY(12);

        private final int months;

        PremiumFrequency(int months) {
            this.months = months;
        }

        public int getMonths() {
            return months;
        }
    }

    /**
     * Exception class for policy-related errors.
     */
    public static class PolicyException extends Exception {
        public PolicyException(String message) {
            super(message);
        }
    }
}

/**
 * Class representing a life insurance policy.
 */
class LifeInsurance extends InsurancePolicy {
    private boolean smoker;
    private double agePriceAdded;

    // Constructor
    public LifeInsurance(String startDateString, CoverageOption coverageOption, PolicyTenure policyTenure,
                         PremiumFrequency premiumFrequency, int age, boolean smoker) throws PolicyException {
        super(startDateString, coverageOption, policyTenure, premiumFrequency, age);
        this.smoker = smoker;
    }

    // Overrides

    @Override
    protected double calculateBasePremium() {
        double basePremium = getCoverageOption().getValue();
        agePriceAdded = getAge() * 10;
        if (smoker) {
            basePremium += 500;
        }
        return basePremium + agePriceAdded;
    }

    @Override
    protected String getPolicyType() {
        return "LIFE";
    }

    // Methods

    /**
     * Checks if the policyholder is a smoker.
     * @return True if the policyholder is a smoker, false otherwise.
     */
    public boolean isSmoker() {
        return smoker;
    }

    /**
     * Gets the additional premium due to age.
     * @return The additional premium due to age.
     */
    public double agePriceAdded(){
        return agePriceAdded;
    }
}

/**
 * Class representing a health insurance policy.
 */
class HealthInsurance extends InsurancePolicy {
    private boolean smoker;
    private double agePriceAdded;

    // Constructor
    public HealthInsurance(String startDateString, CoverageOption coverageOption, PolicyTenure policyTenure,
                           PremiumFrequency premiumFrequency, int age, boolean smoker) throws PolicyException {
        super(startDateString, coverageOption, policyTenure, premiumFrequency, age);
        this.smoker = smoker;
    }

    // Overrides

    @Override
    protected double calculateBasePremium() {
        double basePremium = getCoverageOption().getValue();
        agePriceAdded = getAge() * 10;
        if (smoker) {
            basePremium += 500;
        }
        return basePremium + agePriceAdded;
    }

    @Override
    protected String getPolicyType() {
        return "HEALTH";
    }

    // Methods

    /**
     * Checks if the policyholder is a smoker.
     * @return True if the policyholder is a smoker, false otherwise.
     */
    public boolean isSmoker() {
        return smoker;
    }

    /**
     * Gets the additional premium due to age.
     * @return The additional premium due to age.
     */
    public double agePriceAdded(){
        return agePriceAdded;
    }
}

/**
 * Class representing an accident insurance policy.
 */
class AccidentInsurance extends InsurancePolicy {
    private boolean pastInjuries;
    private double agePriceAdded;

    // Constructor
    public AccidentInsurance(String startDateString, CoverageOption coverageOption, PolicyTenure policyTenure,
                             PremiumFrequency premiumFrequency, int age, boolean pastInjuries) throws PolicyException {
        super(startDateString, coverageOption, policyTenure, premiumFrequency, age);
        this.pastInjuries = pastInjuries;
    }

    // Overrides

    @Override
    protected double calculateBasePremium() {
        double basePremium = getCoverageOption().getValue();
        agePriceAdded = getAge() * 10;
        if (pastInjuries) {
            basePremium += 1000;
        }
        return basePremium + agePriceAdded;
    }
    @Override
    protected String getPolicyType() {
        return "ACCIDENT";
    }

    // Methods

    /**
     * Checks if the policyholder has past injuries.
     * @return True if the policyholder has past injuries, false otherwise.
     */
    public boolean hasPastInjuries() {
        return pastInjuries;
    }

    /**
     * Gets the additional premium due to age.
     * @return The additional premium due to age.
     */
    public double agePriceAdded(){
        return agePriceAdded;
    }
}
