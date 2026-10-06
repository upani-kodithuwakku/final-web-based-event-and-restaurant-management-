package com.group06.restaurantevent.config;

import com.group06.restaurantevent.events.entity.EventHall;
import com.group06.restaurantevent.events.entity.EventPackage;
import com.group06.restaurantevent.events.repository.EventHallRepository;
import com.group06.restaurantevent.events.repository.EventPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

/** Adds the requested celebration catalogue once; existing staff edits are preserved. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.event-catalog", havingValue = "true", matchIfMissing = true)
public class EventCatalogSeeder implements CommandLineRunner {
    private final EventPackageRepository packages;
    private final EventHallRepository halls;
    private record PackageSeed(String name, String type, String description, String price, int min, int max) {}

    @Override
    @Transactional
    public void run(String... args) {
        // ── Halls ──────────────────────────────────────────────────────────────
        hall("Crystal Hall",     200, "Ground Floor", "An elegant setting for weddings, family celebrations and gala dinners.");
        hall("Garden Terrace",    80, "Rooftop",      "An open-air setting for intimate celebrations and evening receptions.");
        hall("Boardroom Suite",   30, "First Floor",  "A private room for smaller gatherings, meetings and team dinners.");
        hall("Sunset Pavilion",  120, "Poolside",     "A stunning poolside pavilion with panoramic views — perfect for cocktail receptions and gala evenings.");

        // ── Celebration packages ────────────────────────────────────────────────
        for (PackageSeed seed : List.of(
            new PackageSeed("Birthday Table for Your Favourite People", "BIRTHDAY", "An intimate birthday dinner with a sharing menu, a mini celebration cake and a decorated table for your closest people.", "12000", 2, 8),
            new PackageSeed("Just the Two of Us", "ANNIVERSARY", "A candlelit anniversary dinner with a three-course menu, a floral table setting and a dessert to share.", "15000", 2, 8),
            new PackageSeed("A Little Family Celebration", "FAMILY", "Bring your closest family together for a Sri Lankan sharing feast, dessert and a relaxed private table.", "18000", 3, 8),
            // Existing packages (preserved if already in DB)
            new PackageSeed("Birthday Garden Party",    "BIRTHDAY",    "A colourful garden gathering with a sharing menu, birthday cake and a decorated celebration table.",                                          "45000",  10,  40),
            new PackageSeed("An Evening to Remember",   "ANNIVERSARY", "Celebrate your story with candlelit dining, a three-course menu and a floral table setting.",                                                "38000",  10,  30),
            new PackageSeed("The Engagement Edit",      "ENGAGEMENT",  "Bring both families together for a welcome reception, a generous buffet and a beautifully styled backdrop.",                                 "125000", 30, 100),
            new PackageSeed("A Beautiful Beginning",    "WEDDING",     "An intimate wedding reception with a welcome drink, curated buffet, floral tables and space for your first dance.",                          "210000", 40, 120),
            new PackageSeed("Little Moments, Big Love", "BABY_SHOWER", "A relaxed afternoon with high-tea favourites, sweet treats and a soft pastel celebration setting.",                                          "42000",  10,  40),
            new PackageSeed("The Graduation Toast",     "GRADUATION",  "A well-earned celebration with a sharing feast, dessert table and a photo corner for your favourite people.",                                "55000",  15,  60),
            new PackageSeed("Together Again",           "FAMILY",      "Make room for everyone with a family-style Sri Lankan feast, a kids-friendly menu and time to reconnect.",                                   "60000",  20,  80),
            new PackageSeed("Beyond the Boardroom",     "CORPORATE",   "A focused team gathering with a meeting setup, refreshments and a three-course dinner to finish the day.",                                   "85000",  10,  30),

            // New packages
            new PackageSeed("Cocktail Reception",       "RECEPTION",   "An elegant stand-up reception with premium canapés, live music-ready space and a curated beverage selection.",                              "70000",  30,  80),
            new PackageSeed("A Fond Farewell",          "FAREWELL",    "Send someone off in style with a heartfelt shared dining experience, personalised touches and a memory book station.",                       "48000",  15,  60),
            new PackageSeed("Christmas Gala Night",     "CHRISTMAS",   "A festive celebration with seasonal décor, a lavish Christmas buffet, mulled punch and a gift exchange corner.",                             "95000",  40, 150),
            new PackageSeed("New Year's Eve Countdown", "NEW_YEAR",    "Ring in the new year with a grand countdown dinner, DJ, confetti moment and a champagne toast at midnight.",                                 "120000", 50, 180),
            new PackageSeed("A Well-Earned Milestone",  "RETIREMENT",  "Honour a lifetime of work with a dignified celebration dinner, tribute video corner and personalised keepsakes.",                           "65000",  20,  70),
            new PackageSeed("Welcome Little One",       "NAMING",      "Celebrate a new arrival with a soft and joyful naming ceremony setting, floral décor and a sweet dessert spread.",                         "50000",  20,  60),
            new PackageSeed("Team Day Out",             "TEAM_BUILDING","Recharge the team with a half-day workshop space, energising lunch and a sunset dinner to wrap up a great day.",                          "75000",  10,  40),
            new PackageSeed("Grand Gala Dinner",        "GALA",        "An opulent black-tie evening with a five-course dinner, live entertainment, floral centrepieces and a red-carpet entry.",                  "180000", 60, 200)
        )) {
            if (!packages.existsByName(seed.name())) {
                packages.save(EventPackage.builder()
                        .name(seed.name()).eventType(seed.type()).description(seed.description())
                        .basePrice(new BigDecimal(seed.price())).minimumGuests(seed.min())
                        .maximumGuests(seed.max()).isActive(true).build());
            }
        }
    }

    private void hall(String name, int capacity, String location, String description) {
        if (!halls.existsByName(name)) halls.save(EventHall.builder().name(name).capacity(capacity)
                .location(location).description(description).isActive(true).build());
    }
}
