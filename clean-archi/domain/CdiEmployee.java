// ====================
// 2. CDIEmployee
// ====================
class CDIEmployee implements Employee {
    private String name;
    private LocalDate contractStartedDate;

    @Override
    public BigDecimal calculateSalary() {
        return new BigDecimal("500000");
    }
}
