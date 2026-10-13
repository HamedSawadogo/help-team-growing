// ====================
// 3. CDDEmployee
// ====================
class CDDEmployee implements Employee {
    private String name;
    private LocalDate contractStartedDate;
    private LocalDate contractEndDate;

    @Override
    public BigDecimal calculateSalary() {
        return new BigDecimal("350000");
    }
}
