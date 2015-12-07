# A very simple Flask Hello World app for you to get started with...
from flask import Flask, request, render_template, Response
import MySQLdb
import json


app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')



@app.route('/robgps', methods=['GET'])
def getCoords():
    rowarray_list = []

    db = MySQLdb.connect(host="robbiescheidt.mysql.pythonanywhere-services.com",    # your host, usually localhost
                     user="robbiescheidt",         # your username
                     passwd=props["mysql_password"],  # your password
                     db="robbiescheidt$robgps")        # name of the data base
    cur = db.cursor(MySQLdb.cursors.DictCursor)
    # 72 should be 12 hours
    cur.execute("""SELECT * FROM records ORDER BY time DESC LIMIT 72;""")
    result_set = cur.fetchall()
    for row in result_set:
        tm = "%s" % row["time"]
        lat = "%s" % row["lat"]
        lng = "%s" % row["lng"]
        t = {"time": tm, "lat":lat, "lng":lng}
        rowarray_list.append(t)

    json_results = json.dumps(rowarray_list)

    db.close()

    resp = Response(json_results, status=200, mimetype='application/json')
    return resp



# http://robbiescheidt.pythonanywhere.com/robgps?lat=37.84410018&lng=-122.29474243
@app.route('/robgps', methods=['POST'])
def call_rest():

    tm = request.form.get("time")
    #currTime = time.strftime('%Y-%m-%d %H:%M:%S')
    lat = request.values.get("lat")
    lng = request.values.get("lng")

    db = MySQLdb.connect(host="robbiescheidt.mysql.pythonanywhere-services.com",    # your host, usually localhost
                     user="robbiescheidt",         # your username
                     passwd=props["mysql_password"],  # your password
                     db="robbiescheidt$robgps")        # name of the data base
    x = db.cursor()
    try:
        x.execute("""INSERT INTO records (time, lat, lng) VALUES (%s,%s,%s)""",(tm, lat, lng))
        db.commit()
    except:
        db.rollback()

    db.close()
    return "success: "


def load_props(filename):
    myprops = {}
    f = open(filename, 'r')
    for line in f:
        line = line.rstrip() #removes trailing whitespace and '\n' chars

        if "=" not in line: continue #skips blanks and comments w/o =
        if line.startswith("#"): continue #skips comments which contain =

        k, v = line.split("=", 1)
        myprops[k] = v
    return myprops


props = load_props('config.properties')