//mongeez formatted javascript
//changeset unlocker:v1_5
db.orders.updateMany({}, {
    "$set": {
        "isElectronic": false
    }
});

db.firms.updateMany({}, {
    "$set": {
        "taxVariant": "GENERAL"
    }
});

db.orders.updateMany({}, {
    "$set": {
        "firm.taxVariant": "GENERAL"
    }
});