package com.acme.hr.config;


import com.acme.hr.entity.Employee;
import com.acme.hr.entity.EmployeeSalary;
import com.acme.hr.repository.EmployeeRepository;
import com.acme.hr.repository.SalaryRepository;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
public class SeedDataConfig {

    private final EmployeeRepository employeeRepo;
    private final SalaryRepository salaryRepo;

    public SeedDataConfig(EmployeeRepository employeeRepo, SalaryRepository salaryRepo) {
        this.employeeRepo = employeeRepo;
        this.salaryRepo = salaryRepo;
    }

    public void seedDatabase() {
            // Use fixed seed for reproducibility
            Random random = new Random(12345);

            // Avoid duplicates: only seed if empty
            if (employeeRepo.count() > 0) {
                System.out.println("Employees already exist, skipping seeding.");
                return;
            }
            String[] firstNames = {"John", "Asha", "Michael", "Priya", "David"};
            String[] lastNames = {"Doe", "Patel", "Smith", "Kumar", "Brown"};

            List<String> countries = Arrays.asList("USA", "India", "Germany", "UK", "Canada");
            Map<String, String> currencyMap = Map.of(
                    "USA", "USD",
                    "India", "INR",
                    "Germany", "EUR",
                    "UK", "GBP",
                    "Canada", "CAD"
            );

            List<String> departments = Arrays.asList("Engineering", "HR", "Finance", "Sales", "Marketing");
            List<String> jobTitles = Arrays.asList("Developer", "Manager", "Analyst", "Consultant", "Intern");

            List<Employee> employees = new ArrayList<>();
            List<EmployeeSalary> salaries = new ArrayList<>();

            for (int i = 1; i <= 10000; i++) {
                String country = countries.get(random.nextInt(countries.size()));
                String department = departments.get(random.nextInt(departments.size()));
                String jobTitle = jobTitles.get(random.nextInt(jobTitles.size()));

                Employee emp = new Employee();
                emp.setEmployeeCode("EMP" + i);
                emp.setFirstName(firstNames[random.nextInt(firstNames.length)]);
                emp.setLastName(lastNames[random.nextInt(lastNames.length)]);
                emp.setEmail("employee" + i + "@example.com");
                emp.setCountry(country);
                emp.setDepartment(department);
                emp.setJobTitle(jobTitle);
                emp.setHireDate(LocalDate.now().minusDays(random.nextInt(3650)));
                emp.setActive(true);

                employees.add(emp);

                // Salary ranges by country
                double baseSalary;
                switch (country) {
                    case "USA" -> baseSalary = random.nextDouble() * (120000 - 50000) + 50000;
                    case "India" -> baseSalary = random.nextDouble() * (1500000 - 300000) + 300000;
                    case "Germany" -> baseSalary = random.nextDouble() * (100000 - 40000) + 40000;
                    case "UK" -> baseSalary = random.nextDouble() * (90000 - 35000) + 35000;
                    case "Canada" -> baseSalary = random.nextDouble() * (95000 - 45000) + 45000;
                    default -> baseSalary = 50000;
                }

                EmployeeSalary sal = new EmployeeSalary();
                sal.setEmployee(emp); // FK mapping via employeeCode
                sal.setBaseSalary(baseSalary);
                sal.setCurrency(currencyMap.get(country));
                sal.setEffectiveDate(LocalDate.now().minusMonths(random.nextInt(60)));
                sal.setUpdatedAt(LocalDateTime.now());

                salaries.add(sal);

                // Batch insert every 500 records
                if (i % 500 == 0) {
                    employeeRepo.saveAll(employees);
                    salaryRepo.saveAll(salaries);
                    employees.clear();
                    salaries.clear();
                    System.out.println("Inserted batch up to employee " + i);
                }
            }
    }
}
