//mongeez formatted javascript
//changeset unlocker:v1_7
db.stores.find({}).forEach(function (store) {
    store.registrars.forEach(function (reg) {
        reg.operatorSite="nalog.ru";
        reg.operatorName="ФНС";
        reg.operatorUser="";
        reg.fiscalStorageNo="";
        reg.registrationNo="";
    });
    db.stores.save(store);
});
