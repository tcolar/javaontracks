function applyFilters()
{
    var pkg=document.getElementById("pkgFilter");
    var item=document.getElementById("itemFilter");
    var childs=document.getElementById("pkgNav").childNodes;
    for (i = 0; i < childs.length; i++)
    {
      alert(childs[i].id);
    }
}