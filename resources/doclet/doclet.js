function showFullDescription(bool)
{
    showElem("shortDesc", ! bool);
    showElem("fullDesc",bool);
}

function showAllSubclasses(bool)
{
    showElem("subClasses", ! bool);
    showElem("allSubClasses",bool);
}

function showElem(id, show)
{
    if(show==true)
        document.getElementById(id).style.display='block';
    else
        document.getElementById(id).style.display='none';
}

function setWindowTitle(title)
{
    window.top.document.title=title;
}

function resetFilters()
{
    document.getElementById("pkgFilter").value="";
    document.getElementById("itemFilter").value="";
    applyFilters();
}

function applyFilters()
{
    var pkg=document.getElementById("pkgFilter").value.toLowerCase();
    var item=document.getElementById("itemFilter").value.toLowerCase();
    var childs=document.getElementById("pkgNav").childNodes;
    for (i = 0; i < childs.length; i++){
        var child=childs[i];
        var id=child.id;
        if(id!=null && id.indexOf("__")==0){
            if(id.toLowerCase().indexOf(pkg)==-1){
                child.style.display='none';
            }
            else{
                var anyChild=false;
                var items=child.childNodes;
                for (j = 0; j < items.length; j++){
                    var it=items[j];
                    var iid=it.id;
                    if(iid!=null && iid.indexOf(id+"__")==0){
                        iid=iid.substring(id.length);
                        if(iid.toLowerCase().indexOf(item)==-1)
                            it.style.display='none';
                        else{
                            anyChild=true;
                            it.style.display='block';
                        }
                    }
                } // end item loop
                if(anyChild)
                    child.style.display='block';
                else
                    child.style.display='none';
            } // end visible package
        }
    }
}

