var express = require('express');
var multer = require('multer');
var crypto = require('crypto');
var router = express.Router();
var _storage = multer.diskStorage({
    destination: 'uploads/',
    filename: function(req, file, cb) {
        return crypto.pseudoRandomBytes(16, function(err, raw) {
            if (err) {
                return cb(err);
            }
            return cb(null, file.originalname);
        });
    }
});
var app = express();
const { spawn } = require('child_process');

router.post('/upload',
    multer({
        storage: _storage
    }).single('upload'),
    function(req, res) {

        try {
            let file = req.file;
            let originalName = '';
            let fileName = '';
            let mimeType = '';
            let size = 0;

            if (file) {
                originalName = file.originalname;
                fileName = file.filename;
                mimeType = file.mimetype;
                size = file.size;
                console.log("execute : " + fileName);
            } else {
                console.log("request is null");
            }

        } catch (err) {

            console.dir(err.stack);
        }

        let fn = req.file.filename;
        let srcLang = req.body.src;
        let dstLang = req.body.dst;

        console.log(fn);
        console.log(srcLang);
        console.log(dstLang);

        const pyProcess = spawn('python', ['./OCRTranslator.py', fn, srcLang, dstLang]); // 파이썬 실행

        pyProcess.stdout.on('data', function(data) { // 파이썬의 print 후 sys.stdout.flush() 된 데이터에 대해서
            console.log(data.toString());
            res.write(data);
        });

        //res.write("SHS");

        return res.status(200).end();
    });



router.get('/get', function(req, res, next) {
    console.log('GET 호출 / data : ' + req.query.data);
    console.log('path : ' + req.path);
    res.send('get success')
});

router.put('/put/:id', function(req, res, next) {
    console.log('UPDATE 호출 / id : ' + req.params.id);
    console.log('body : ' + req.body.data);
    console.log('path : ' + req.path);
    res.send('put success')
});

router.delete('/delete/:id', function(req, res, next) {
    console.log('DELETE 호출 / id : ' + req.params.id);
    console.log('path : ' + req.path);
    res.send('delete success')
});

module.exports = router;