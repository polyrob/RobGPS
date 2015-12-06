from flask import Flask, request, render_template
import MySQLdb
import time

app = Flask(__name__)


@app.route('/')
def index():
    return render_template('index.html')


# http://robbiescheidt.pythonanywhere.com/robgps?lat=37.84410018&lng=-122.29474243
@app.route('/robgps', methods=['POST'])
def call_rest():

    print(request.values)

    # time = request.form.get("time")
    currTime = time.strftime('%Y-%m-%d %H:%M:%S')
    lat = request.values.get("lat")
    lng = request.values.get("lng")

    print(currTime, lat, lng)

    db = MySQLdb.connect(host="robbiescheidt.mysql.pythonanywhere-services.com",    # your host, usually localhost
                     user="robbiescheidt",         # your username
                     passwd="#######",  # your password
                     db="###########")        # name of the data base

    x = db.cursor()


    try:
        x.execute("""INSERT INTO records (time, lat, lng) VALUES (%s,%s,%s)""",(currTime, lat, lng))
        db.commit()
    except:
        db.rollback()

    db.close()

    return "success: "