
//Require
const functions = require('firebase-functions');
const admin = require('firebase-admin');

try {admin.initializeApp();} catch(e) {}

//Database Reference
var db = admin.database();
var userRef = db.ref('user');

exports.onCreateData = functions.database.ref('rawData/{machineNum}/{timeStamp}')
  .onCreate(snap => {
    if (!snap.val()) {
      console.log('No data!');
      return false
    }
    const machineNum = snap.val().machine_num;
    const goodsCount = snap.val().goods;
    const coinCount = snap.val().coin;

    userRef.child(machineNum).child('token').on('value', function(snap) {
      sendMessage(snap.val(), goodsCount, coinCount);
    });

    return true
  });

  //發送訊息
  function sendMessage(recipient, goods, coin) {
    if (recipient == null) { return }

    var messageTitle = "抓寶曼斯特";
    var messageBody = "出貨數：" + goods + "，投幣數：" + coin;

    //APNs推播格式
    /*var message = {
      
      apns: {
        headers: {
          'apns-priority': '10'
        },
        payload: {
          aps: {
            alert: {
              title: messageTitle,
              body: messageBody,
            },
            sound: 'default',
          }
        }
      },
      token: recipient
    };*/

    //android 推播格式
    var message = {
      android: {
        ttl: 3600 * 1000, // 1 hour in milliseconds
        priority: 'normal',
        notification: {
          title: messageTitle,
          body: messageBody,
          icon: 'stock_ticker_update',
          color: '#f45342'
        }
      },
      token: recipient
    };

    //寄送推播後的回應顯示在控制台
    admin.messaging().send(message)
      .then((response) => {
        // Response is a message ID string.
        console.log('Message send: ', recipient, ',ID: ', response);
      })
      .catch((error) => {
        console.log('Error sending message:', error);
      });
  }