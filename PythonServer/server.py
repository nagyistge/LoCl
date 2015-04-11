from flask import Flask, request, redirect, url_for,send_from_directory
from werkzeug import secure_filename
from flask import render_template
import os


UPLOAD_FOLDER = os.environ['FLASK_DOWNLOAD_PATH']
 
ALLOWED_EXTENSIONS = set(['txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'])

app = Flask(__name__)
app.debug = True
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

print "Upload Folder is " + app.config['UPLOAD_FOLDER']


@app.route('/hello/')
@app.route('/hello/<name>')
def hello(name=None):
    return render_template('hello.html', name=name)

@app.route('/')
def hello_world():
    return 'Hello World!'

@app.route('/user/<username>')
def show_user_profile(username):
    # show the user profile for that user
    return 'User %s' % username


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS

@app.route('/upload', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        file = request.files['file']
        if file and allowed_file(file.filename):
            name = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], name))
            print "file saved"
            return redirect(url_for('uploaded_file',filename=name))
        else:
            return 'filetype not allowed'
    return render_template('upload.html') 

@app.route('/uploadMobile', methods=['GET', 'POST'])
def uploadMobile():
    print "Request from mobile"
    imageFolder = request.form['ImageFolder']
    print imageFolder
    if request.method == 'POST':
        print "POST Request from mobile"
        file = request.files['file']
        if file and allowed_file(file.filename):
            print "In Allowed Request from mobile"
            name = secure_filename(file.filename)
            print "file name is " + name
            imageCreatePath = app.config['UPLOAD_FOLDER'] + imageFolder
            if not os.path.exists(imageCreatePath):
               os.makedirs(imageCreatePath)
               print "Directory created : " + imageCreatePath
            file.save(os.path.join(app.config['UPLOAD_FOLDER'] + imageFolder, name))
            imgUrl=url_for('uploaded_file',filename=name)
            return imgUrl
        else:
            return 'filetype not allowed'
    return render_template('upload.html') 


@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'],
                               filename)
@app.route('/about')
def about():
    # show the user profile for that user
    return 'Write about the server and its version and previous dev versions'

if __name__ == '__main__':
   app.run(host='0.0.0.0')

