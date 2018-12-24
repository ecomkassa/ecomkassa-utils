//mongeez formatted javascript
//changeset unlocker:v1_8
db.orders.update(
   {},
   {
       $rename: { "fiscalData.transferCode": "fiscalData.registrationNo" }
   },
   { multi: true }
);
