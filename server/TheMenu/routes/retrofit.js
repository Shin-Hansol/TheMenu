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

router.get('/get', function(req, res, next) {
    console.log('GET 호출 / data : ' + req.query.data);
    console.log('path : ' + req.path);
    res.send('get success')
});

router.post('/post', function(req, res, next) {
    console.log('POST 호출 / data : ' + req.body.data);
    console.log('path : ' + req.path);
    res.send('post success')
});

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
                filename = file.fileName;
                mimeType = file.mimetype;
                size = file.size;
                console.log("execute" + fileName);
            } else {
                console.log("request is null");
            }

        } catch (err) {

            console.dir(err.stack);
        }

        console.log(req.file);
        console.log(req.body);
        res.redirect("/uploads/" + req.file.originalname);

        return res.status(200).end();
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