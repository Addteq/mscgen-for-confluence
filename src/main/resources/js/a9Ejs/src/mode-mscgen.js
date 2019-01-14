/*
Mode and highlighter for Mscgen, used in Mscgen-JIRA plugin.
Author: Mian Yang
*/

define('ace/mode/mscgen', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text', 'ace/tokenizer', 'ace/mode/mscgen_highlight_rules', 'ace/range'], function(require, exports, module) {


var oop = require("../lib/oop");
var TextMode = require("./text").Mode;
var Tokenizer = require("../tokenizer").Tokenizer;
var MscgenHighlightRules = require("./mscgen_highlight_rules").MscgenHighlightRules;
var Range = require("../range").Range;

var Mode = function() {
    this.HighlightRules = MscgenHighlightRules;
};
oop.inherits(Mode, TextMode);

(function() {

    this.lineCommentStart = "--";

    this.$id = "ace/mode/mscgen";
}).call(Mode.prototype);

exports.Mode = Mode;

});

define('ace/mode/mscgen_highlight_rules', ['require', 'exports', 'module' , 'ace/lib/oop', 'ace/mode/text_highlight_rules'], function(require, exports, module) {


var oop = require("../lib/oop");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

var MscgenHighlightRules = function() {
  
      var keywordMapper = this.createKeywordMapper({
        "variable.language": "msc",
        "keyword": 
            " label URL ID IDURL arcskip linecolour linecolor textcolour textcolor textbgcolour"+
            " arclinecolour arclinecolor arctextcolour arctextcolor arctextbgcolour arctextbgcolor",
        "constant.language": 
            "TRUE FALSE NULL SPACE",
        "support.type": 
            "box abox rbox note "
    }, "text", true, " ");

    this.$rules = {
        "start" : [
            {token : "comment",  
                regex : /#.+|#/},
            {token : "keyword.operator", 
                regex: /=|[->]|[<-]|[<=]|[=>]|[<<]|[>>]|[-x]|[x-]|[=>>]|[<<=]|[...]|[---]|[|||]|[*<-]|[->*]/},
            {token : "keyword.parameter", 
                regex : /"[^"]+"/}, 
            {token : keywordMapper, 
                regex : "\\b\\w+\\b"},
            {caseInsensitive: true}
        ]
    };

};

oop.inherits(MscgenHighlightRules, TextHighlightRules);

exports.MscgenHighlightRules = MscgenHighlightRules;
});
