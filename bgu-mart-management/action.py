from persistence import *

import sys


def act(splittedline):

    productId = int(splittedline[0])
    quantity = int(splittedline[1])
    activatorId = int(splittedline[2])
    date = splittedline[3]

    product = repo.products.find(id = productId)
    if not product:
        print ("Product not found")
        return

    product = product[0]
    if quantity != 0:  
        if quantity < 0:
            # Sale
            if product.quantity >= abs(quantity):
                product.quantity += quantity
                cmd = "UPDATE products SET quantity = ? WHERE id = ?"
                repo._conn.execute(cmd , (product.quantity,product.id))
        # Supply    
        else:  
            product.quantity += quantity
            cmd = "UPDATE products SET quantity = ? WHERE id = ?"
            repo._conn.execute(cmd , (product.quantity,product.id))
    
    activity  = Activitie(product_id=productId, quantity=quantity, activator_id=activatorId, date=date)
    repo.activities.insert(activity)
        


def main(args : list[str]):
    inputfilename : str = args[1]
    with open(inputfilename) as inputfile:
        for line in inputfile:
            splittedline : list[str] = line.strip().split(", ")
            #TODO: apply the action (and insert to the table) if possible
            act(splittedline)


if __name__ == '__main__':
    main(sys.argv)