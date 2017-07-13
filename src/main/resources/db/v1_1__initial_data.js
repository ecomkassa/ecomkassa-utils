//mongeez formatted javascript
//changeset unlocker:v1_1
db.silhouette.user.insert({
	"_id" : ObjectId("5956a3f445b6602afd8df775"),
	"userID" : "aeadceec-7dcc-4b1f-9351-2236f0aa5977",
	"loginInfo" : {
		"providerID" : "credentials",
		"providerKey" : "test@test.test"
	},
	"firstName" : "test",
	"lastName" : "test",
	"fullName" : "test test",
	"email" : "test@test.test",
	"activated" : true,
	"blocked" : false,
	"adminRoles" : [ ],
	"firmRoles" : [ ]
});
db.silhouette.password.insert({
    "_id" : {
        "providerID" : "credentials",
        "providerKey" : "test@test.test"
    },
    "hasher" : "bcrypt",
    "password" : "$2a$10$IUopjoKrfQl6rtkSmZnDse/FSrQqAD3rokomhw963Lfgw/3cRoee2"
});
