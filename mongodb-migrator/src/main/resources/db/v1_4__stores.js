//mongeez formatted javascript
//changeset unlocker:v1_4

db.stores.insert({
    "_id": 1,
    "firmID" : "aeadceec-7dcc-4b1f-9351-2236f0aa6001",
    "name": "Вундер-Маркет",
    "address": "123456, г. Тмутараканск, ул. Еловая, 8А",
    "timezone": "UTC",
    "registrars": [{
        "registrarID": "2705dfee-7781-11e7-b413-c7fc6cd36f17",
        "model": "ШТРИХ-М ФРК",
        "serialNo": "71621",
        "ipAddress": ""
    }]
});

db.sequences.insert({"_id": "stores", "seq": 1});
db.sequences.insert({"_id": "regIssues", "seq": 0});

// cleans up old orders
db.orders.remove({});