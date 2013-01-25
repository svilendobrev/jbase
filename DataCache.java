package com.svilendobrev.jbase;

//import com.svilendobrev.jbase.Model;
import com.svilendobrev.jbase.funk;
import java.util.HashMap;

abstract public class DataCache {
    static public interface DataLoader {
        public Model.Many loadData();
    }

    boolean _dirty = true;
    protected Model.Many data;

    //public DataCache() {}
    public Model.Many getData( DataLoader loader) {
        if (_dirty) {
            data = loader!=null ? loader.loadData() : loadData();
            _clearmap();
            for (Model m : data)
                _add2map( m);
            _dirty = false;
        }
        return data;
    }
    public Model.Many getData() { return getData(null); }

    public void update_now() {
        invalidate();
        getData();
    }
    public void invalidate() {
        _dirty = true;
    }

    public boolean dirty() { return _dirty; }

    public void add( Model m) {
        if (m == null) return;
        data.add( m);
        _add2map( m);
    }
    public void remove( Model m) {
        data.remove( m);
        _del2map(m);
    }

    abstract protected void _clearmap();
    abstract protected void _add2map( Model m);
    abstract protected void _del2map( Model m);
    abstract public Model getById( long id);
    abstract public Model getById( String key);

    protected Model.Many loadData() { return null; } //either redefine this or use a DataLoader


    public static class DataCacheInt extends DataCache {
        private HashMap< Long, Model> map = new HashMap();

        @Override protected void _clearmap() { map.clear(); }
        @Override protected void _add2map( Model m) { map.put( m.getId(), m); }
        @Override protected void _del2map( Model m) { map.remove( m.getId()); }
        @Override public Model getById( long id) { return map.get( id); }
        @Override public Model getById( String key) { funk.fail("only integer keys supported"); return null; }
    }
    public static class DataCacheStr extends DataCache {
        private HashMap< String, Model> map = new HashMap();

        @Override protected void _clearmap() { map.clear(); }
        @Override protected void _add2map( Model m) { map.put( m.getKey(), m); }
        @Override protected void _del2map( Model m) { map.remove( m.getKey()); }
        @Override public Model getById( String key) { return map.get( key); }
        @Override public Model getById( long id) { funk.fail("only string keys supported"); return null; }
    }
}


// vim:ts=4:sw=4:expandtab
