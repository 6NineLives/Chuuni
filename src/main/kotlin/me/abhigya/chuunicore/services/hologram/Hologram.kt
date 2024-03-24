package me.abhigya.chuunicore.services.hologram

import me.abhigya.chuunicore.model.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Hologram(
    val key: String,
    val pool: HologramPool,
    location: Location
) {

    var location: Location = location
        get() = field.clone()
        private set
    
    private val _hologramPages: MutableList<HologramPage> = Collections.synchronizedList(ArrayList(1))
    val hologramPages: List<HologramPage> get() = _hologramPages.toList()
    
    private val _viewerPages: MutableMap<UUID, Int> = ConcurrentHashMap()
    val viewerPages: Map<UUID, Int> get() = _viewerPages.toMap()
    
    internal val viewers: MutableSet<UUID> = Collections.synchronizedSet(HashSet())
    internal val outOfRenderDistance: MutableSet<UUID> = Collections.synchronizedSet(HashSet())
    
    private var _clickActions: MutableSet<ClickEvent> = Collections.synchronizedSet(HashSet())
    val clickActions: Set<ClickEvent> get() = _clickActions.toSet()
    
    var displayRange: Int = HOLOGRAM_DEFAULT_DISPLAY_RANGE
    var updateRange: Int = HOLOGRAM_DEFAULT_DISPLAY_RANGE
    var updateInterval: Duration = 20.seconds

    var isInverted: Boolean = false
        set(value) {
            field = value
            this.teleport(location)
        }

    internal var hasChangedContentType: Boolean = false
    var isClickRegistered: Boolean = false
    var isUpdateRegistered: Boolean = false
    var lastUpdate: Long = System.currentTimeMillis()
        internal set
    internal val tracker: HologramTracker = HologramTracker(this)

    init {
        tracker.start()
    }

    fun addPage(): HologramPage {
        val hologramPage = HologramPage(pool, _hologramPages.size, this)
        _hologramPages.add(hologramPage)
        return hologramPage
    }

    fun changeViewerPage(viewer: Player, page: Int) {
        if (!this.isVisible(viewer)) return
        val currentPage: HologramPage? = this.getCurrentPage(viewer)
        if (currentPage != null) {
            pool.respawnHologram(this, viewer) {
                _viewerPages[viewer.uniqueId] = page
            }
        } else {
            _viewerPages[viewer.uniqueId] = page
            pool.spawnHologram(this, viewer)
        }
    }

    fun changeViewerPage(page: Int) {
        for (viewer in viewers) {
            changeViewerPage(Bukkit.getPlayer(viewer) ?: continue, page)
        }
    }

    fun getCurrentPage(viewer: Player): HologramPage? {
        val i = _viewerPages.getOrDefault(viewer.uniqueId, -1)
        return if (i != -1) _hologramPages[i] else null
    }

    fun addClickAction(action: ClickEvent) {
        _clickActions.add(action)
    }

    fun isVisible(player: Player): Boolean {
        return viewers.contains(player.uniqueId)
    }

    fun show(vararg players: Player) {
        for (player in players) {
            if (this.isVisible(player)) continue
            viewers.add(player.uniqueId)
            pool.spawnHologram(this, player)
        }
    }

    fun showNearby(range: Int = displayRange) {
        location.getNearbyPlayers(range.toDouble()).forEach { this.show(it) }
    }

    fun updateContent(vararg players: Player) {
        for (player in players) {
            pool.updateContent(this, player)
        }
    }

    fun updateLocation(vararg players: Player) {
        for (player in players) {
            pool.updateLocation(this, player)
        }
    }

    fun hide(vararg players: Player) {
        for (player in players) {
            pool.despawnHologran(this, player)
            _viewerPages.remove(player.uniqueId)
            viewers.remove(player.uniqueId)
        }
    }

    fun hideAll() {
        val players: MutableList<Player> = ArrayList()
        for (viewer in viewers) {
            val player = Bukkit.getPlayer(viewer)
            if (player != null) players.add(player)
            else {
                viewers.remove(viewer)
                _viewerPages.remove(viewer)
            }
        }
        this.hide(*players.toTypedArray<Player>())
    }

    fun destroy() {
        hideAll()
        tracker.stop()
        pool.holograms.remove(this.key)
    }

    fun teleport(location: Location) {
        this.location = location
        pool.updateLocation(this)
    }

}