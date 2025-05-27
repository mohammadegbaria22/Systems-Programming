from persistence import *

def printAllTables():
    #activities 
    activities = repo.execute_command("SELECT * FROM activities ORDER BY date")
    print("Activities")
    print()
    for act in activities:
        print(act)


    #branches
    branches = repo.branches.find_all()
    print("Branches")
    print()
    for branch in branches:
        print(branch)


    #employees 
    print("Employees")
    print()
    employees = repo.employees.find_all()
    for emp in employees:
        print(emp)


    #products
    products = repo.products.find_all()
    print("Products")
    print()
    for product in products:
        print(product)


    #suppliers
    suppliers = repo.suppliers.find_all()
    print("Suppliers")
    print()
    for supplier in suppliers:
        print(supplier)




def printEmpReport():
    print("Employees report")
    print()
    detailed_employees = repo.execute_command("""
    SELECT e.name, e.salary, b.location, COALESCE(SUM(p.price * a.quantity),0) AS total_sales_income
    FROM employees e
    JOIN branches b ON e.branche = b.id
    LEFT JOIN activities a ON e.id = a.activator_id
    LEFT JOIN products p ON p.id = a.product_id
    GROUP BY e.id, e.name, e.salary, b.location
    ORDER BY e.name ASC;
    """)
    for emp in detailed_employees:
        formatted_output = f"{emp[0]} {emp[1]} {emp[2]} {abs(emp[3])}"
        print(formatted_output)






def print_activity_report():
    print("Activities report")
    print()
    detailed_activities = repo.execute_command("""
    SELECT a.date, p.description, a.quantity,
        CASE WHEN a.quantity < 0 THEN e.name ELSE NULL END AS seller,
        CASE WHEN a.quantity > 0 THEN s.name ELSE NULL END AS supplier
    FROM activities a
    LEFT JOIN employees e ON e.id = a.activator_id
    LEFT JOIN suppliers s ON s.id = a.activator_id
    JOIN products p ON p.id = a.product_id
    ORDER BY a.date
    """)
    for act in detailed_activities:
        print(act)



def main():
    printAllTables()
    print()
    printEmpReport()
    print()
    print_activity_report()

if __name__ == '__main__':
    main()